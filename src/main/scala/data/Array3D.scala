package com.puffin.data

import scala.collection.mutable.ListBuffer

import com.puffin.Common._

object Array3D {

  def initWith[T: Manifest](dimSize: Int, constructor: () => T): Array3D[T] = {
    initWith(dimSize, dimSize, dimSize, constructor)
  }

  def initWith[T: Manifest](dimX: Int, dimY: Int, dimZ: Int, constructor: () => T): Array3D[T] = {
    val data = new Array3D[T](dimX, dimY, dimZ)
    for (x <- 0 until dimX; y <- 0 until dimY; z <- 0 until dimZ)
      data.put(x, y, z, constructor())
    data
  }
}

class Array3D[T: Manifest] (val dimX: Int, val dimY: Int, val dimZ: Int) extends Iterable[T] {
  def this(dimSize: Int) = this(dimSize, dimSize, dimSize)

  val elems = dimX * dimY * dimZ
  val data = new Array[T](elems)
  
  def get(x: Int, y: Int, z: Int) =
    data(clamp(x, dimX-1) + clamp(y, dimY-1)*dimX + clamp(z, dimZ-1)*dimX*dimY)

  def put(x: Int, y: Int, z: Int, value: T) =
    data(clamp(x, dimX-1) + clamp(y, dimY-1)*dimX + clamp(z, dimZ-1)*dimX*dimY) = value

  def iterator = (for ((x, y, z) <- xyzIn(0, dimX, dimY, dimZ)) yield get(x, y, z)).iterator

  def iteratorWithKey[U](f:(Point, T) => U) = 
    (for ((x, y, z) <- xyzIn(0, dimX, dimY, dimZ)) yield f((x, y, z), get(x, y, z))).iterator

  // Gets adjacent neighbours
  def getNeighbours(x: Int, y: Int, z: Int) = {
    val ns: ListBuffer[(Int,Int,Int)] = new ListBuffer()
    if (x > 0) ns += ((x - 1, y, z))
    if (y > 0) ns += ((x, y - 1, z))
    if (z > 0) ns += ((x, y, z - 1))

    if (x < dimX - 1) ns += ((x + 1, y, z))
    if (y < dimY - 1) ns += ((x, y + 1, z))
    if (z < dimZ - 1) ns += ((x, y, z + 1))
    ns
  }

}
