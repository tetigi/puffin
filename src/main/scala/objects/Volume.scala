package com.puffin.objects

import scala.util.control.Breaks._
import scala.collection.mutable.ListBuffer
import scala.math._

import com.puffin.Common._
import com.puffin.render.Quads
import com.puffin.simplex.SimplexNoise

object Array3D {
  def initWith[T: Manifest](size: Int, constructor: () => T) = {
    val data = new Array3D[T](size)
    for (x <- 0 until size; y <- 0 until size; z <- 0 until size)
      data.put(x, y, z, constructor())
    data
  }
}

class Array3D[T: Manifest] (val size: Int) {
  val elems = size * size * size
  val data = new Array[T](elems)
  
  def get(x: Int, y: Int, z: Int) =
    data(clamp(x, size-1) + clamp(y, size-1)*size + clamp(z, size-1)*size*size)

  def put(x: Int, y: Int, z: Int, value: T) =
    data(clamp(x, size-1) + clamp(y, size-1)*size + clamp(z, size-1)*size*size) = value
}

class Volume(val size: Int) extends Quads {
  val data = new Array3D[Int](size)
  def get(x: Int, y: Int, z: Int) = data.get(x, y, z)
  def put(x: Int, y: Int, z: Int, v: Int) = data.put(x, y, z, v)

  def fillRandom(p: Double = 0.5) = {
    //Pick random cells to fill
    val fill = 
      (for {
        x <- 1 until size -1
        y <- 1 until size -1
        z <- 1 until size -1
      } yield (x, y, z, random)) filter { _._4 <= clamp(p, 0, 1) }
    fill map { x => put(x._1, x._2, x._3, 1) }
    ()
  }

  def fillSimplexNoise(lim: Double) = {
    val fill = 
      (for {
        x <- 1 until size -1
        y <- 1 until size -1
        z <- 1 until size -1
        nx = x.toFloat / size.toFloat
        ny = y.toFloat / size.toFloat
        nz = z.toFloat / size.toFloat
      } yield (x, y, z, SimplexNoise.simplexNoise(1, nx*3, ny*3, nz*3))) filter { _._4 > lim }
    fill map { x => put(x._1, x._2, x._3, 1) }
    ()
  }

  def fillFloatingRock() = {
    println("Filling with floating rock...")
    var progress = 0
    for {
        x <- 1 until size -1
        y <- 1 until size -1
        z <- 1 until size -1
        xf = x.toFloat / size.toFloat
        yf = y.toFloat / size.toFloat
        zf = z.toFloat / size.toFloat
    } {
      if (progress % (size*size*size/10) == 0) 
        println(s"${progress*100/(size*size*size)}% complete...")

      progress += 1
      var plateauFalloff = 0.0
      if (yf <= 0.8) plateauFalloff = 1.0
      else if (0.8 < yf && yf < 0.9) plateauFalloff = 1.0 - (yf - 0.8)*10.0

      val centerFalloff = 0.1/(
        pow((xf-0.5)*1.5, 2) +
        pow((yf-1.0)*0.8, 2) +
        pow((zf-0.5)*1.5, 2))

      var density = SimplexNoise.simplexNoise(5, xf, yf*0.5, zf) *
        centerFalloff * plateauFalloff
      density *= pow(
        SimplexNoise.noise((xf+1)*3.0, (yf+1)*3.0, (zf+1)*3.0) + 0.4, 1.8)
      
      put(x, y, z, if (density > 3.1) 1 else 0)
    }
    println("...Done!")
  }

  def fillIsland() = {
    println("Filling with island...")
    var progress = 0
    for {
        x <- 1 until size -1
        y <- 1 until size -1
        z <- 1 until size -1
        xf = x.toFloat / size.toFloat
        yf = y.toFloat / size.toFloat
        zf = z.toFloat / size.toFloat
    } {
      if (progress % (size*size*size/10) == 0) 
        println(s"${progress*100/(size*size*size)}% complete...")

      progress += 1
      var plateauFalloff = 0.0
      if (0.4 <= yf && yf <= 0.5) plateauFalloff = 1.0
      else if (0.5 < yf && yf < 0.6) plateauFalloff = 1.0 - (yf - 0.6)*10.0

      val centerFalloff = 0.1/(
        pow((xf-0.5)*1.5, 2) +
        pow((yf-0.5)*1.5, 2) +
        pow((zf-0.5)*1.5, 2))

      var density = SimplexNoise.simplexNoise(5, xf, yf*0.5, zf) *
        centerFalloff * plateauFalloff
      density *= pow(
        SimplexNoise.noise((xf+1)*3.0, (yf+1)*3.0, (zf+1)*3.0) + 0.4, 1.8)
      
      put(x, y, z, if (density > 3.1) 1 else 0)
    }
    println("...Done!")
  }

  // Gets adjacent neighbours
  def getNeighbours(x: Int, y: Int, z: Int) = {
    val ns: ListBuffer[(Int,Int,Int)] = new ListBuffer()
    if (x > 0) ns += ((x - 1, y, z))
    if (y > 0) ns += ((x, y - 1, z))
    if (z > 0) ns += ((x, y, z - 1))

    if (x < size - 1) ns += ((x + 1, y, z))
    if (y < size - 1) ns += ((x, y + 1, z))
    if (z < size - 1) ns += ((x, y, z + 1))
    ns
  }

  def createRawQuadData(opts: RenderOptions): RawQuadData = {
    var quadVerts: ListBuffer[Float] = new ListBuffer()
    var normals: ListBuffer[Float] = new ListBuffer()
    var occlusion: ListBuffer[Float] = new ListBuffer()

    val cells = if (opts.occlusionEnabled) getOcclusions() else Array3D.initWith(size, { () => new Cell(0)})
    println("Getting raw quads...")
    var progress = 0

    for {
      x <- 0 until this.size
      y <- 0 until this.size
      z <- 0 until this.size
      } {
        if (progress % (size*size*size/10) == 0) 
          println(s"${progress*100/(size*size*size)}% complete...")
        progress += 1
        if (this.get(x,y,z) == 0) {
          val thisCell = cells.get(x, y, z) 
          for ((nx,ny,nz) <- this.getNeighbours(x,y,z) if this.get(nx,ny,nz) != 0) {
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
    quadVerts = quadVerts.map( _ / this.size).map(_ - 0.5f)

    println("...Done!")
    new RawQuadData(quadVerts.toArray, normals.toArray, occlusion.toArray)
  }

  def getOcclusions(): Array3D[Cell] = {
    val occlusions = Array3D.initWith(size, { () => new Cell()})

    println("Getting occlusions for faces...")
    var progress = 0

    val sample = new Sample()
    for {
      x <- 0 until size
      y <- 0 until size
      z <- 0 until size
    } {
      if (progress % (size*size*size/10) == 0) 
        println(s"${progress*100/(size*size*size)}% complete...")
      progress += 1
      val value = get(x, y, z)
      if (value == 0 && !getNeighbours(x, y, z).isEmpty) {
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
              if (xoff < 0 || xoff >= size) break
              else if (yoff < 0 || yoff >= size) break
              else if (zoff < 0 || zoff >= size) break
              else if (get(xoff, yoff, zoff) != 0) {
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

class Cell (var left: Float, var right: Float, var top: Float, var bottom: Float, var front: Float, var back: Float){
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
