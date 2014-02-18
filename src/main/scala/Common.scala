package com.puffin

import org.lwjgl.util.vector.Vector3f
import org.lwjgl.util.vector.Matrix3f

import scala.math._

object Common {
  def clamp(x: Int, ulim: Int) = 
    min(ulim, x)

  def clamp(x: Int, llim: Int, ulim: Int) = 
    max(llim, min(ulim, x))

  def clamp(x: Double, llim: Double, ulim: Double) = 
    max(llim, min(ulim, x))

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

    def setOcclusionEnable(b: Boolean) = {
      occlusionEnabled = b
    }
  }
}
