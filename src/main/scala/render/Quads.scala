package com.puffin.render

import java.nio.FloatBuffer
import java.nio.IntBuffer

import org.lwjgl.BufferUtils
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL15
import org.lwjgl.opengl.GL20
import org.lwjgl.opengl.GL30

import scala.math._
import scala.collection.mutable.ListBuffer
import scala.util.control.Breaks._

import com.puffin.context._
import com.puffin.render._
import com.puffin.Common._
import com.puffin.data.Array3D

trait Quads extends RenderableBase {
  var rawQuadCache: RawQuadData = null

  def render(opts: RenderOptions) = {
    if (rawQuadCache == null) 
      rawQuadCache = createRawQuadData(opts)
    renderQuads(rawQuadCache)
  }

  def getData: Array3D[Int]
  def getDimSize: Int

  private def createRawQuadData(opts: RenderOptions): RawQuadData = {
    var quadVerts: ListBuffer[Float] = new ListBuffer()
    var normals: ListBuffer[Float] = new ListBuffer()
    var occlusion: ListBuffer[Float] = new ListBuffer()

    val data = getData
    val dimSize = getDimSize

    val cells = 
      if (opts.occlusionEnabled) getOcclusions() 
      else Array3D.initWith(dimSize, { () => new OccludeCell(0)})
    println("Getting raw quads...")
    var progress = 0

    for {
      (x, y, z) <- xyzIn(0, dimSize)
      } {
        if (progress % (dimSize*dimSize*dimSize/10) == 0) 
          println(s"${progress*100/(dimSize*dimSize*dimSize)}% complete...")
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
            if (dx != 0) { // Left or right neighbour 
              occlusion.append(if (dx < 0) thisCell.right else thisCell.left) 
              occlusion.append(if (dx < 0) thisCell.right else thisCell.left) 
              occlusion.append(if (dx < 0) thisCell.right else thisCell.left) 
              occlusion.append(if (dx < 0) thisCell.right else thisCell.left) 
              normals.appendAll(List(dx * 2, 0, 0))
              normals.appendAll(List(dx * 2, 0, 0))
              normals.appendAll(List(dx * 2, 0, 0))
              normals.appendAll(List(dx * 2, 0, 0))
              quadVerts.appendAll(List(
                nx + dx, ny + d, nz + d,
                nx + dx, ny - d, nz + d,
                nx + dx, ny - d, nz - d,
                nx + dx, ny + d, nz - d))
            } else if (dy != 0) { // Top or bottom neighbour
              occlusion.append(if (dy < 0) thisCell.top else thisCell.bottom) 
              occlusion.append(if (dy < 0) thisCell.top else thisCell.bottom) 
              occlusion.append(if (dy < 0) thisCell.top else thisCell.bottom) 
              occlusion.append(if (dy < 0) thisCell.top else thisCell.bottom) 
              normals.appendAll(List(0, dy * 2, 0))
              normals.appendAll(List(0, dy * 2, 0))
              normals.appendAll(List(0, dy * 2, 0))
              normals.appendAll(List(0, dy * 2, 0))
              quadVerts.appendAll(List(
                nx + d, ny + dy, nz + d,
                nx + d, ny + dy, nz - d,
                nx - d, ny + dy, nz - d,
                nx - d, ny + dy, nz + d))
            } else if (dz != 0) { // Front or back neighbour
              occlusion.append(if (dz < 0) thisCell.front else thisCell.back) 
              occlusion.append(if (dz < 0) thisCell.front else thisCell.back) 
              occlusion.append(if (dz < 0) thisCell.front else thisCell.back) 
              occlusion.append(if (dz < 0) thisCell.front else thisCell.back) 
              normals.appendAll(List(0, 0, dz * 2))
              normals.appendAll(List(0, 0, dz * 2))
              normals.appendAll(List(0, 0, dz * 2))
              normals.appendAll(List(0, 0, dz * 2))
              quadVerts.appendAll(List(
                nx + d, ny + d, nz + dz,
                nx - d, ny + d, nz + dz,
                nx - d, ny - d, nz + dz,
                nx + d, ny - d, nz + dz))
            }
          }
        }
    }

    // Rescale the verts so that they're centered around the origin and 1x1x1
    quadVerts = quadVerts.map( _ / dimSize).map(_ - 0.5f)

    println("...Done!")
    new RawQuadData(quadVerts.toArray, normals.toArray, occlusion.toArray)
  }

  def getOcclusions(): Array3D[OccludeCell] = {
    val dimSize = getDimSize
    val data = getData
    val occlusions = Array3D.initWith(dimSize, { () => new OccludeCell(0)})

    println("Getting occlusions for faces...")
    var progress = 0

    val sample = new Sample()
    for {
      (x, y, z) <- xyzIn(0, dimSize)
    } {
      if (progress % (dimSize*dimSize*dimSize/10) == 0) 
        println(s"${progress*100/(dimSize*dimSize*dimSize)}% complete...")
      progress += 1
      val value = data.get(x, y, z)
      if (value == 0 && !data.getNeighbours(x, y, z).isEmpty) {
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
              if (xoff < 0 || xoff >= dimSize) break
              else if (yoff < 0 || yoff >= dimSize) break
              else if (zoff < 0 || zoff >= dimSize) break
              else if (data.get(xoff, yoff, zoff) != 0) {
                collided = true
                break
              }
            }
          }
          if (!collided) cell.addRay(ray)
        }
        cell.normalize(sample)
      }
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
    GL20.glEnableVertexAttribArray(Context.vertexAttribArray)

    GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, Context.vboOcclusionId)
    GL15.glBufferData(GL15.GL_ARRAY_BUFFER, quads.occlusionBuffer, GL15.GL_STATIC_DRAW)
    GL20.glVertexAttribPointer(Context.occlusionAttribArray, 1, GL11.GL_FLOAT, false, 0, 0)
    GL20.glEnableVertexAttribArray(Context.normalAttribArray)

    GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, Context.vboIndicesId)
    GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, quads.indicesBuffer, GL15.GL_STATIC_DRAW)
    GL20.glEnableVertexAttribArray(Context.occlusionAttribArray)
      
    GL11.glDrawElements(GL11.GL_TRIANGLES, quads.indices.length, GL11.GL_UNSIGNED_INT, 0)
    GL30.glBindVertexArray(0)
  }


  class RawQuadData (val verts: Array[Float], val normals: Array[Float], val occlusion: Array[Float]) {
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
  def generateIndices(n: Int, start: Int = 0, indices: List[Int] = Nil): List[Int] =
    if (n == 0) indices.reverse
    else generateIndices(n - 6, start + 4, start :: (start + 1) :: (start + 2) :: (start + 2) :: (start + 3) :: start :: indices)
}

class Sample() {
  val RAY_COUNT = 1024
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
  val POINT_COUNT = 128
  var left, right, top, bottom, front, back = 0f
  val points: ListBuffer[Offset] = new ListBuffer()

  // Generates a ray in a grid
  def toGrid(x: Float, y: Float, z: Float) = {
    // direction to move towards
    val (scaledX, scaledY, scaledZ) = (x * 0.2, y * 0.2, z * 0.2)
    var newX, newY, newZ = 0.0 
    var cx, cy, cz = 0
    val points: ListBuffer[(Float, Int, Int, Int)] = new ListBuffer()
    
    while (points.length < POINT_COUNT) {
      val (ncx, ncy, ncz) = (newX.toInt, newY.toInt, newZ.toInt)
      if (ncx != cx || ncy != cy || ncz != cz) {
        val depth = sqrt(newX*newX + newY*newY + newZ*newZ).toFloat
        cx = ncx; cy = ncy; cz = ncz
        points += ((depth, cx, cy, cz))
      }
      newX += scaledX; newY += scaledY; newZ += scaledZ
    }

    points
  }

  // Compute the path of the ray from origin to this point
  def compute(x: Float, y: Float, z: Float) = {
    for ((depth, xoff, yoff, zoff) <- toGrid(x, y, z))
      points += new Offset(depth, xoff, yoff, zoff)

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
