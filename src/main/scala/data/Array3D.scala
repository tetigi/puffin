package com.puffin.data

import scala.collection.mutable.ListBuffer

import com.puffin.Common._

object Array3D {
  def initWith[T: Manifest](dimSize: Int, constructor: () => T) = {
    val data = new Array3D[T](dimSize)
    for (x <- 0 until dimSize; y <- 0 until dimSize; z <- 0 until dimSize)
      data.put(x, y, z, constructor())
    data
  }
}

class Array3D[T: Manifest] (val dimSize: Int) extends Iterable[T] {
  val elems = dimSize * dimSize * dimSize
  val data = new Array[T](elems)
  
  def get(x: Int, y: Int, z: Int) =
    data(clamp(x, dimSize-1) + clamp(y, dimSize-1)*dimSize + clamp(z, dimSize-1)*dimSize*dimSize)

  def put(x: Int, y: Int, z: Int, value: T) =
    data(clamp(x, dimSize-1) + clamp(y, dimSize-1)*dimSize + clamp(z, dimSize-1)*dimSize*dimSize) = value

  def iterator = (for ((x, y, z) <- xyzIn(0, dimSize)) yield get(x, y, z)).iterator

  def iteratorWithKey[U](f:(Point, T) => U) = 
    (for ((x, y, z) <- xyzIn(0, dimSize)) yield f((x, y, z), get(x, y, z))).iterator

  // Gets adjacent neighbours
  def getNeighbours(x: Int, y: Int, z: Int) = {
    val ns: ListBuffer[(Int,Int,Int)] = new ListBuffer()
    if (x > 0) ns += ((x - 1, y, z))
    if (y > 0) ns += ((x, y - 1, z))
    if (z > 0) ns += ((x, y, z - 1))

    if (x < dimSize - 1) ns += ((x + 1, y, z))
    if (y < dimSize - 1) ns += ((x, y + 1, z))
    if (z < dimSize - 1) ns += ((x, y, z + 1))
    ns
  }

}
