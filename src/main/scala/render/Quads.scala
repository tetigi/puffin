package com.puffin.render

import java.nio.FloatBuffer
import java.nio.IntBuffer

import org.lwjgl.BufferUtils
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL15
import org.lwjgl.opengl.GL20
import org.lwjgl.opengl.GL30
import org.lwjgl.util.vector.Vector3f

import scala.math._
import scala.collection.mutable.ListBuffer
import scala.util.control.Breaks._
import scala.annotation.tailrec

import scala.concurrent.duration._
import scala.concurrent.Await
import scala.concurrent.Future
import scala.concurrent.future
import scala.concurrent.ExecutionContext.Implicits.global

import com.puffin.context._
import com.puffin.render._
import com.puffin.Common._
import com.puffin.data.Array3D
import com.puffin.utils.Model

trait Quads extends RenderableBase {
  var rawQuadCache: RawQuadData = new RawQuadData()

  var refreshPending = false
  var futureRender: Option[Future[RawQuadData]] = None

  def render(opts: RenderOptions) = {
    if (requiresRefresh) {
      if (futureRender.isEmpty) { // Start new refresh job
        futureRender = Some(future {
            createRawQuadData(opts)
          })
      } else { // Check on current job
        if (futureRender.get.isCompleted) { // it's done - update our cache
          rawQuadCache = Await.result(futureRender.get, 1.millis)
          requiresRefresh = false
          futureRender = None
        }
      }
    }
    renderQuads(rawQuadCache)
  }

  def getData: Array3D[Int]
  def getUsedPoints: Iterable[Point3]
  def getDims: (Int, Int, Int)
  def getPosition: Point3

  // I want this method to be spawned as a separate task
  private def createRawQuadData(opts: RenderOptions): RawQuadData = {
    var quadVerts: ListBuffer[Vector3f] = new ListBuffer()
    var normals: ListBuffer[Vector3f] = new ListBuffer()
    var occlusion: ListBuffer[Float] = new ListBuffer()

    val (worldX, worldY, worldZ) = World.size

    val data = Array3D.pad(getData)
    val (dimX, dimY, dimZ) = data.getDims
    val position = getPosition

    val cells = 
      if (opts.occlusionEnabled) getOcclusions() 
      else Array3D.initWith(dimX, dimY, dimZ, { () => new OccludeCell(0)})
    println("Getting raw quads...")
    var progress = 0
    
    // TODO Have 2 options -> turn it into a padded cube and continue as before,
    //      or change the logic to account for calls from outside the cube

    for {
      (x, y, z) <- xyzIn(0, dimX, dimY, dimZ)
      } {
        if (progress % max(1, dimX*dimY*dimZ/10) == 0) 
          println(s"${progress*100/(dimX*dimY*dimZ)}% complete...")
        progress += 1
        if (data.get(x,y,z) == 0) {
          val thisCell = cells.get(x, y, z) 
          for ((nx,ny,nz) <- data.getNeighbours(x,y,z) if data.get(nx,ny,nz) != 0) {
            // Generate quad
            val d = 0.5f
            val dx: Float = (x.toFloat - nx) / 2.0f
            val dy: Float = (y.toFloat - ny) / 2.0f
            val dz: Float = (z.toFloat - nz) / 2.0f
            // negative dx/y/z means neighbour is on the right, top, front
            // TODO Convert to list of vectors and then flatmap across them to get the stream
            // This way I can map the transformations nicely
            if (dx != 0) { // Left or right neighbour 
              occlusion.appendAll(repeat(if (dx < 0) thisCell.right else thisCell.left, 4))
              normals.appendAll(repeat(new Vector3f(dx * 2, 0, 0), 4))
              quadVerts.appendAll(List( // Don't forget to normalize to 1x1x1!
                new Vector3f(nx + dx, ny + d, nz + d),
                new Vector3f(nx + dx, ny - d, nz + d),
                new Vector3f(nx + dx, ny - d, nz - d),
                new Vector3f(nx + dx, ny + d, nz - d)))
            } else if (dy != 0) { // Top or bottom neighbour
              occlusion.appendAll(repeat(if (dy < 0) thisCell.top else thisCell.bottom, 4))
              normals.appendAll(repeat(new Vector3f(0, dy * 2, 0), 4))
              quadVerts.appendAll(List(
                new Vector3f(nx + d, ny + dy, nz + d),
                new Vector3f(nx + d, ny + dy, nz - d),
                new Vector3f(nx - d, ny + dy, nz - d),
                new Vector3f(nx - d, ny + dy, nz + d)))
            } else if (dz != 0) { // Front or back neighbour
              occlusion.appendAll(repeat(if (dz < 0) thisCell.front else thisCell.back, 4))
              normals.appendAll(repeat(new Vector3f(0, 0, dz * 2), 4))
              quadVerts.appendAll(List(
                new Vector3f(nx + d, ny + d, nz + dz),
                new Vector3f(nx - d, ny + d, nz + dz),
                new Vector3f(nx - d, ny - d, nz + dz),
                new Vector3f(nx + d, ny - d, nz + dz)))
            }
          }
        }
    }
    println("Mapping, flattening and resizing...")
    
    // Move the points to their proper locations
    println("Moving...")
    quadVerts.map({ v: Vector3f => Vector3f.add(v, position.toVector3f, v)})

    // Rescale the verts so that they're centered around the origin and 1x1x1
    println("Resizing...")
    quadVerts.map({ v: Vector3f => flatScaleVector3f(v, new Vector3f(1.0f/worldX, 1.0f/worldY, 1.0f/worldZ), v) })
    //quadVerts.map({ v: Vector3f => flatScaleVector3f(v, new Vector3f(1.0f/dimX, 1.0f/dimY, 1.0f/dimZ), v) })
    //quadVerts.map({ v: Vector3f => Vector3f.add(v, new Vector3f(-0.5f, -0.5f, -0.5f), v)})
    println("Flattening verts...")
    val flatQuadVerts = quadVerts.flatMap({ v: Vector3f => List(v.x, v.y, v.z) })
    

    // Duplicate and flatten the normal vectors
    println("Flattening normals...")
    val flatNormals = normals.flatMap({ v: Vector3f => List(v.x, v.y, v.z) })

    // Duplicate the occlusion paramaters 4 times for each vertex

    println("...Done!")
    new RawQuadData(flatQuadVerts.toArray, flatNormals.toArray, occlusion.toArray)
  }

  def getOcclusions(): Array3D[OccludeCell] = {
    val data = Array3D.pad(getData)
    val (dimX, dimY, dimZ) = data.getDims
    println(data.getDims)
    val occlusions = Array3D.initWith(dimX, dimY, dimZ, { () => new OccludeCell(0)})

    println("Getting occlusions for faces...")
    var progress = 0

    val sample = new Sample()
    var x, y, z = 0
    while (x < dimX) { y = 0
      while (y < dimY) { z = 0
        while (z < dimZ) { 
          if (progress % max(1, dimZ*dimY*dimZ/10) == 0) 
            println(s"${progress*100/(dimX*dimY*dimZ)}% complete...")
          progress += 1
          val value = data.get(x, y, z)
          if (value == 0 && data.hasNeighbourEqual(x, y, z, 1)) {
            val cell = occlusions.get(x, y, z)
            for (ray <- sample.rays) {
              var collided = false
              breakable {
                //TODO Might be dodgy
                //for (off <- ray.points) {
                val points = ray.points
                for (off <- ray.points) {
                  val xoff = off.x + x
                  val yoff = off.y + y
                  val zoff = off.z + z
                  if (xoff < 0 || xoff >= dimX) break
                  else if (yoff < 0 || yoff >= dimY) break
                  else if (zoff < 0 || zoff >= dimZ) break
                  //else if (data.get(xoff, yoff, zoff) != 0) {
                  else if (World.getOccupiedRelative(this, xoff - 1, yoff - 1, zoff - 1)) {
                    collided = true
                    break
                  }
                }
              }
              if (!collided) cell.addRay(ray)
            }
            cell.normalize(sample)
          }
          z += 1
        }
        y += 1
      }
      x += 1
    }
    println("...Done!")
    occlusions
  }

  private def renderQuads(quads: RawQuadData) = {
    // SETUP QUAD

    GL30.glBindVertexArray(Context.vaoId)
    GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, Context.vboVertexId)
    GL15.glBufferData(GL15.GL_ARRAY_BUFFER, quads.vertBuffer, GL15.GL_STATIC_DRAW)
    GL20.glVertexAttribPointer(Context.vertexAttribArray, 3, GL11.GL_FLOAT, false, 0, 0)
    // Place these in the attributes for the shader

    GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, Context.vboNormalId)
    GL15.glBufferData(GL15.GL_ARRAY_BUFFER, quads.normalBuffer, GL15.GL_STATIC_DRAW)
    GL20.glVertexAttribPointer(Context.normalAttribArray, 3, GL11.GL_FLOAT, false, 0, 0)
    GL20.glEnableVertexAttribArray(Context.normalAttribArray)

    GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, Context.vboOcclusionId)
    GL15.glBufferData(GL15.GL_ARRAY_BUFFER, quads.occlusionBuffer, GL15.GL_STATIC_DRAW)
    GL20.glVertexAttribPointer(Context.occlusionAttribArray, 1, GL11.GL_FLOAT, false, 0, 0)
    GL20.glEnableVertexAttribArray(Context.occlusionAttribArray)

    GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, Context.vboIndicesId)
    GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, quads.indicesBuffer, GL15.GL_STATIC_DRAW)
    GL20.glEnableVertexAttribArray(Context.vertexAttribArray)
      
    GL11.glDrawElements(GL11.GL_TRIANGLES, quads.indices.length, GL11.GL_UNSIGNED_INT, 0)
    GL30.glBindVertexArray(0)
  }


  class RawQuadData (val verts: Array[Float], val normals: Array[Float], val occlusion: Array[Float]) {
    def this() = this(new Array(0), new Array(0), new Array(0))

    val vertBuffer = BufferUtils.createFloatBuffer(verts.length)
    vertBuffer.put(verts)
    vertBuffer.flip()
    val normalBuffer = BufferUtils.createFloatBuffer(normals.length)
    normalBuffer.put(normals)
    normalBuffer.flip()
    val indices = QuadUtils.generateIndices(verts.length / 2)
    val indicesBuffer = BufferUtils.createIntBuffer(indices.length)
    indicesBuffer.put(indices.toArray)
    indicesBuffer.flip()
    val occlusionBuffer = BufferUtils.createFloatBuffer(occlusion.length)
    occlusionBuffer.put(occlusion)
    occlusionBuffer.flip()
  }
}

object QuadUtils {
  @tailrec
  def generateIndices(n: Int, start: Int = 0, indices: List[Int] = Nil): List[Int] =
    if (n == 0) indices.reverse
    else generateIndices(n - 6, start + 4, start :: (start + 1) :: (start + 2) :: (start + 2) :: (start + 3) :: start :: indices)
}

class Sample() {
  val RAY_COUNT = 128
  var left, right, top, bottom, front, back = 0f
  val rays = List.fill(RAY_COUNT)(new Ray()) 

  def pointsOnSphere(n: Int) = {
    val inc = Pi * (3.0 - sqrt(5))
    val off = 2.0 / n
    for {
      k <- 0 until n
      y = k.toFloat * off - 1.0 + (off / 2.0)
      r = sqrt(1.0 - y*y)
      phi = k.toFloat * inc
    } yield ((cos(phi) * r).toFloat, y.toFloat, (sin(phi)*r).toFloat)
  }

  for { // For every point on the sphere compute a ray
    (ray, (x, y, z)) <- rays.zip(pointsOnSphere(rays.length))
  } {
    ray.compute(x, y, z)
    left  += ray.left;   right  += ray.right
    top   += ray.top;    bottom += ray.bottom
    front += ray.front; back += ray.back
  }

}

class Ray() {
  val POINT_COUNT = 32
  var left, right, top, bottom, front, back = 0f
  val points: ListBuffer[Offset] = new ListBuffer()

  // Generates a ray in a grid
  def toGrid(x: Float, y: Float, z: Float) = {
    // direction to move towards
    val (scaledX, scaledY, scaledZ) = (x * 0.2, y * 0.2, z * 0.2)
    var newX, newY, newZ = 0.0 
    var cx, cy, cz = 0
    val points: ListBuffer[Offset] = new ListBuffer()
    
    while (points.length < POINT_COUNT) {
      val (ncx, ncy, ncz) = (newX.toInt, newY.toInt, newZ.toInt)
      if (ncx != cx || ncy != cy || ncz != cz) {
        val depth = sqrt(newX*newX + newY*newY + newZ*newZ).toFloat
        cx = ncx; cy = ncy; cz = ncz
        points += new Offset(depth, cx, cy, cz)
      }
      newX += scaledX; newY += scaledY; newZ += scaledZ
    }

    points
  }

  // Compute the path of the ray from origin to this point
  def compute(x: Float, y: Float, z: Float) = {
    points.appendAll(toGrid(x, y, z))
    // Lamberts law per side
    right  = if (x < 0) -x else 0.0f
    left   = if (x > 0) x else 0.0f
    top    = if (y < 0) -y else 0.0f 
    bottom = if (y > 0) y else 0.0f
    front  = if (z < 0) -z else 0.0f
    back   = if (z > 0) z else 0.0f
  }
}

class Offset(val depth: Float, val x: Int, val y: Int, val z: Int) {
}

class OccludeCell (var left: Float, var right: Float, var top: Float, var bottom: Float, var front: Float, var back: Float){
  def this() = this(0, 0, 0, 0, 0, 0)
  def this(v: Float) = this(v, v, v, v, v, v)
  //def this() = this(1, 1, 1, 1, 1, 1)
  def addRay(ray: Ray) = {
    left  += ray.left;  right  += ray.right
    top   += ray.top;   bottom += ray.bottom
    front += ray.front; back   += ray.back
  }

  def normalize(sample: Sample) = {
    left = 1 - left / sample.left
    right = 1 - right / sample.right
    top = 1 - top / sample.top
    bottom = 1 - bottom / sample.bottom
    front = 1 - front / sample.front
    back = 1 - back / sample.back
  }
  
  override def toString = s"Left: $left, Right: $right, Top: $top, Bottom: $bottom, Front: $front, Back: $back"
}
