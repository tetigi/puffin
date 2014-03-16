package com.puffin

import org.lwjgl.util.vector.Vector3f
import org.lwjgl.util.vector.Matrix3f

import scala.math._
import scala.collection.mutable.ListBuffer

object Common {
  case class Point2(var x: Int, var y: Int) {
    def this() = this(0, 0)
    def this(p: Tuple2[Int, Int]) = this(p._1, p._2)
    def toTuple = (x, y)
    def +(that: Point2) =
      new Point2(this.x + that.x, this.y + that.y)
    override def hashCode = 41 * (41 + x) + y
    override def equals(other : Any) : Boolean = other match {
      case that : Point2 => 
        this.x == that.x &&
        this.y == that.y
      case _ => false
    }
  }

  case class Point3(var x: Int, var y: Int, var z: Int) {
    def this() = this(0, 0, 0)
    def this(p: Tuple3[Int, Int, Int]) = this(p._1, p._2, p._3)

    def toVector3f = new Vector3f(x.toFloat, y.toFloat, z.toFloat)
    def toTuple = (x, y, z)
    def toTupleF = (x.toFloat, y.toFloat, z.toFloat)
    def +(that: Point3) =
      new Point3(this.x + that.x, this.y + that.y, this.z + that.z)

    override def hashCode = 41 * (41 * (41 + x) + y) + z
    override def equals(other : Any) : Boolean = other match {
      case that : Point3 => 
        this.x == that.x &&
        this.y == that.y &&
        this.z == that.z
      case _ => false
    }

    def set(that: Point3) {
      this.x = that.x
      this.y = that.y
      this.z = that.z
    }
  }

  def clamp(x: Int, ulim: Int) = 
    min(ulim, x)

  def clamp(x: Int, llim: Int, ulim: Int) = 
    max(llim, min(ulim, x))

  def clamp(x: Double, llim: Double, ulim: Double) = 
    max(llim, min(ulim, x))

  def repeat[T](elem: T, n: Int) = {
    List.iterate(elem, n)({ x => x })
  }

  def xzIn(start: Int, end: Int): IndexedSeq[Tuple2[Int, Int]] = 
    xzIn(start, end, end)

  def xzIn(start: Int, endX: Int, endZ: Int): IndexedSeq[Tuple2[Int, Int]] =
    for (x <- start until endX; z <- start until endZ) yield (x, z)

  def xzIn(startX: Int, startZ: Int, endX: Int, endZ: Int): IndexedSeq[Tuple2[Int, Int]] = 
    for (x <- startX until endX; z <- startZ until endZ) yield (x, z)

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

  def zipWith[T](f: (T, T) => T, xs: List[T], ys: List[T]): List[T] =
    xs.zip(ys).map({ case (x, y) => f(x, y) })

  class RenderOptions() {
    var occlusionEnabled = true
    var statusEnabled = false

    def setStatusEnabled(b: Boolean) {
      statusEnabled = b
    }

    def setOcclusionEnabled(b: Boolean) {
      occlusionEnabled = b
    }

  }
}
