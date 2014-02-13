package com.puffin.objects

import scala.collection.mutable.ListBuffer
import scala.math._

import com.puffin.Common._
import com.puffin.render.RawQuads

class Volume(val size: Int) {
  val data = new Array[Int](size*size*size)
  
  def get(x: Int, y: Int, z: Int) =
    data(clamp(x, size-1) + clamp(y, size-1)*size + clamp(z, size-1)*size*size)

  def put(x: Int, y: Int, z: Int, value: Int) =
    data(clamp(x, size-1) + clamp(y, size-1)*size + clamp(z, size-1)*size*size) = value

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

  def getRawQuads() = {
    var quadVerts: ListBuffer[Float] = new ListBuffer()
    var normals: ListBuffer[Float] = new ListBuffer()
    for {
      x <- 0 until this.size
      y <- 0 until this.size
      z <- 0 until this.size
      } {
        if (this.get(x,y,z) == 0) {
          for ((nx,ny,nz) <- this.getNeighbours(x,y,z) if this.get(nx,ny,nz) != 0) {
            // Generate quad
            val d = 0.5f
            val dx: Float = (x.toFloat - nx) / 2.0f
            val dy: Float = (y.toFloat - ny) / 2.0f
            val dz: Float = (z.toFloat - nz) / 2.0f
            if (dx != 0) { 
              normals.appendAll(List(dx * 2, 0, 0))
              quadVerts.appendAll(List(
                nx + dx, ny + d, nz - d,
                nx + dx, ny - d, nz - d,
                nx + dx, ny - d, nz + d,
                nx + dx, ny + d, nz + d))
            } else if (dy != 0) {
              normals.appendAll(List(0, dy * 2, 0))
              quadVerts.appendAll(List(
                nx + d, ny + dy, nz - d,
                nx - d, ny + dy, nz - d,
                nx - d, ny + dy, nz + d,
                nx + d, ny + dy, nz + d))
            } else if (dz != 0) {
              normals.appendAll(List(0, 0, dz * 2))
              quadVerts.appendAll(List(
                nx + d, ny - d, nz + dz,
                nx - d, ny - d, nz + dz,
                nx - d, ny + d, nz + dz,
                nx + d, ny + d, nz + dz))
            }
          }
        }
    }

    // Rescale the verts so that they're centered around the origin and 1x1x1
    quadVerts = quadVerts.map( _ / this.size).map(_ - 0.5f)

    new RawQuads(quadVerts.toArray, normals.toArray)
  }
}
