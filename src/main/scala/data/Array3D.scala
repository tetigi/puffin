package com.puffin.data

import scala.collection.mutable.ListBuffer

import com.puffin.Common._

object Array3D {

  def pad[T: Manifest](data: Array3D[T]) = {
    val ret = new Array3D(data.dimX + 2, data.dimY + 2, data.dimZ + 2)
    for ((p, v) <- data.iteratorWithKey)
      ret.put(p.x + 1, p.y + 1, p.z + 1, v)
    ret
  }

  def initWith[T: Manifest](dimSize: Int, constructor: () => T): Array3D[T] = {
    initWith(dimSize, dimSize, dimSize, constructor)
  }

  def initWith[T: Manifest](dimX: Int, dimY: Int, dimZ: Int, constructor: () => T): Array3D[T] = {
    val data = new Array3D[T](dimX, dimY, dimZ)
    for (x <- 0 until dimX; y <- 0 until dimY; z <- 0 until dimZ)
      data.put(x, y, z, constructor())
    data
  }

  // Gets adjacent neighbours - x y and z can take any value
  def getNeighbours(x: Int, y: Int, z: Int, dimX: Int, dimY: Int, dimZ: Int) = {
    val ns: ListBuffer[(Int,Int,Int)] = new ListBuffer()
    if (y >= 0 && z >= 0 && y < dimY && z < dimZ) {
      if (x > 0 && x <= dimX ) ns += ((x - 1, y, z))
      if (x >= -1 && x < dimX - 1) ns += ((x + 1, y, z))
    }

    if (x >= 0 && z >= 0 && x < dimX && z < dimZ) {
      if (y > 0 && y <= dimY ) ns += ((x, y - 1, z))
      if (y >= -1 && y < dimY - 1) ns += ((x, y + 1, z))
    }

    if (y >= 0 && x >= 0 && y < dimY && x < dimX) {
      if (z > 0 && z <= dimZ ) ns += ((x, y, z - 1))
      if (z >= -1 && z < dimZ - 1) ns += ((x, y, z + 1))
    }
    ns
  }
}

class Array3D[@specialized(Int) T: Manifest] (val dimX: Int, val dimY: Int, val dimZ: Int) extends Iterable[T] {
  def this(dimSize: Int) = this(dimSize, dimSize, dimSize)
  def getDims() = (dimX, dimY, dimZ)

  val elems = dimX * dimY * dimZ
  val data = new Array[T](elems)
  
  def get(x: Int, y: Int, z: Int) =
    data(clamp(x, dimX-1) + clamp(y, dimY-1)*dimX + clamp(z, dimZ-1)*dimX*dimY)

  def put(x: Int, y: Int, z: Int, value: T) =
    data(clamp(x, dimX-1) + clamp(y, dimY-1)*dimX + clamp(z, dimZ-1)*dimX*dimY) = value

  def putAll(value: T) =
    for ((x, y, z) <- xyzIn(0, dimX, dimY, dimZ)) put(x, y, z, value)

  def iterator = (for ((x, y, z) <- xyzIn(0, dimX, dimY, dimZ)) yield get(x, y, z)).iterator

  def iteratorWithKey = 
    (for ((x, y, z) <- xyzIn(0, dimX, dimY, dimZ)) yield (new Point(x, y, z), get(x, y, z))).iterator

  // Returns a new Array3D
  def map[U: Manifest](f: T => U) = {
    val out: Array3D[U] = new Array3D[U](dimX, dimY, dimZ)
    for ((p, v) <- iteratorWithKey) out.put(p.x, p.y, p.z, f(v))
    out
  }

  def getNeighbours(x: Int, y: Int, z: Int) = 
    Array3D.getNeighbours(x, y, z, dimX, dimY, dimZ)

  // x y and z can take any value
  def hasNeighbourEqual[T](x: Int, y: Int, z: Int, v: T) = {
    var has = false
    if (y >= 0 && z >= 0 && y < dimY && z < dimZ) {
      if (x > 0 && x <= dimX) has = has || get(x - 1, y, z) == v
      if (x >= -1 && x < dimX - 1) has = has || get(x + 1, y, z) == v
    }

    if (x >= 0 && z >= 0 && x < dimX && z < dimZ) {
      if (y > 0 && y <= dimY) has = has || get(x, y - 1, z) == v
      if (y >= -1 && y < dimY - 1) has = has || get(x, y + 1, z) == v
    }

    if (y >= 0 && x >= 0 && y < dimY && x < dimX) {
      if (z > 0 && z <= dimZ) has = has || get(x, y, z - 1) == v
      if (z >= -1 && z < dimZ - 1) has = has || get(x, y, z + 1) == v
    }
    has
  }

  def copy(that: Array3D[T]) {
    for ((x, y, z) <- xyzIn(0, dimX, dimY, dimZ))
      this.put(x, y, z, that.get(x, y, z))
  }
}
