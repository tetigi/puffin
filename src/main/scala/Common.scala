package com.puffin

import org.lwjgl.util.vector.Vector3f
import org.lwjgl.util.vector.Matrix3f

import scala.math._
import scala.collection.mutable.ListBuffer

object Common {
  class Point(var x: Int, var y: Int, var z: Int) {
    def this() = this(0, 0, 0)
    def this(p: Tuple3[Int, Int, Int]) = this(p._1, p._2, p._3)

    def toVector3f = new Vector3f(x.toFloat, y.toFloat, z.toFloat)
    def toTuple = (x, y, z)
    def toTupleF = (x.toFloat, y.toFloat, z.toFloat)
    def +(that: Point) =
      new Point(this.x + that.x, this.y + that.y, this.z + that.z)
  }

  def clamp(x: Int, ulim: Int) = 
    min(ulim, x)

  def clamp(x: Int, llim: Int, ulim: Int) = 
    max(llim, min(ulim, x))

  def clamp(x: Double, llim: Double, ulim: Double) = 
    max(llim, min(ulim, x))

  def repeatEachElem[T](elems: ListBuffer[T], times: Int) = 
    if (elems.isEmpty) elems
    else {
      val out: ListBuffer[T] = new ListBuffer()
      for (i <- 0 until elems.length)
        for (j <- 0 until times)
          out += elems(i)
      out
    }

  def xzIn(start: Int, end: Int): IndexedSeq[Tuple2[Int, Int]] = 
    xzIn(start, end, end)

  def xzIn(start: Int, endX: Int, endZ: Int): IndexedSeq[Tuple2[Int, Int]] =
    for (x <- start until endX; z <- start until endZ) yield (x, z)

  def xyzIn(start: Int, end: Int): IndexedSeq[Tuple3[Int, Int, Int]] = xyzIn(start, end, end, end)

  def xyzIn(start: Int, endX: Int, endY: Int, endZ: Int): IndexedSeq[Tuple3[Int, Int, Int]] = {
    for {
      x <- start until endX
      y <- start until endY
      z <- start until endZ
    } yield (x, y, z)
  }

  def readFileAsString(filename: String) = {
    val source = scala.io.Source.fromFile(filename)
    val lines = source.mkString
    source.close()
    lines
  }

  def toRadiansF(degrees: Double) =
    toRadians(degrees).toFloat

  def scaleVector3f(vec: Vector3f, fac: Float, dest: Vector3f) = {
    dest.set(vec.x * fac, vec.y * fac, vec.z * fac)
    dest
  }

  def flatScaleVector3f(vec: Vector3f, facV: Vector3f, dest: Vector3f) = {
    dest.x = vec.x * facV.x
    dest.y = vec.y * facV.y
    dest.z = vec.z * facV.z
    dest
  }

  def getRotateY(theta: Float) = {
    val matrix = new Matrix3f()
    matrix.m00 = cos(theta).toFloat; matrix.m02 = sin(theta).toFloat
    matrix.m20 = -sin(theta).toFloat; matrix.m22 = cos(theta).toFloat
    matrix
  }

  def getRotateZ(theta: Float) = {
    val matrix = new Matrix3f()
    matrix.m00 = cos(theta).toFloat; matrix.m01 = -sin(theta).toFloat
    matrix.m10 = sin(theta).toFloat; matrix.m11 = cos(theta).toFloat
    matrix
  }

  def rotateY(v: Vector3f, theta: Float) = {
    val matrix = getRotateY(theta)

    Matrix3f.transform(matrix, v, v)
  }
  
  def rotateZ(v: Vector3f, theta: Float) = {
    val matrix = getRotateZ(theta)

    Matrix3f.transform(matrix, v, v)
  }

  class RenderOptions() {
    var occlusionEnabled = true

    def setOcclusionEnabled(b: Boolean) = {
      occlusionEnabled = b
    }

  }
}
