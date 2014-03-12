package com.puffin.data

import scala.collection.mutable.ListBuffer

import com.puffin.Common._

object Array2D {
  def initWith[T: Manifest](dimSize: Int, constructor: () => T): Array2D[T] = {
    initWith(dimSize, dimSize, constructor)
  }

  def initWith[T: Manifest](dimX: Int, dimY: Int, constructor: () => T): Array2D[T] = {
    val data = new Array2D[T](dimX, dimY)
    for (x <- 0 until dimX; y <- 0 until dimY)
      data.put(x, y, constructor())
    data
  }
}

class Array2D[@specialized(Int) T: Manifest] (val dimX: Int, val dimY: Int) extends Iterable[T] {
  def this(dimSize: Int) = this(dimSize, dimSize)
  def getDims() = (dimX, dimY)

  val elems = dimX * dimY
  val data = new Array[T](elems)
  
  def get(x: Int, y: Int) =
    data(clamp(x, dimX-1) + clamp(y, dimY-1)*dimX)

  def put(x: Int, y: Int, value: T) =
    data(clamp(x, dimX-1) + clamp(y, dimY-1)*dimX) = value

  def putAll(value: T) =
    for ((x, y) <- xzIn(0, dimX, dimY)) put(x, y, value)

  def iterator = (for ((x, y) <- xzIn(0, dimX, dimY)) yield get(x, y)).iterator

  def iteratorWithKey =
    (for ((x, y) <- xzIn(0, dimX, dimY)) yield (new Point2(x, y), get(x, y))).iterator

  // Returns a new Array2D
  def map[U: Manifest](f: T => U) = {
    val out: Array2D[U] = new Array2D[U](dimX, dimY)
    for ((p, v) <- iteratorWithKey) out.put(p.x, p.y, f(v))
    out
  }

  def copy(that: Array2D[T]) {
    for ((x, y) <- xzIn(0, dimX, dimY))
      this.put(x, y, that.get(x, y))
  }
}
