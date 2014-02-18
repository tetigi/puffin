package com.puffin

import com.puffin.utils.Camera
import com.puffin.utils.Model

import org.lwjgl.util.vector.Matrix4f
import org.lwjgl.util.vector.Vector3f

import scala.math._

package object utils { 

  class UniformLocations(val pvmLoc: Int, val normalMatrixLoc: Int) {}

  class Matrices(val viewMatrix: Matrix4f, val modelMatrix: Matrix4f, val projectionMatrix: Matrix4f) {}

  class Transmogrifiers(val camera: Camera, val model: Model) {}

  def setupMatrices(width: Int, height: Int) = {
    val projectionMatrix = new Matrix4f()
    val fieldOfView = 80f
    val aspectRatio = width.toFloat / height.toFloat
    val nearPlane = 0.1f
    val farPlane = 100f

    def cotan(x: Double) = 1.0 / tan(x)
    val top = nearPlane * tan((Pi / 180) * fieldOfView / 2f).toFloat
    val bottom = -top
    val right = top * aspectRatio
    val left = -right

    projectionMatrix.m00 = 2 * nearPlane / (right - left)
    projectionMatrix.m11 = 2 * nearPlane / (top - bottom)
    projectionMatrix.m20 = (right + left) / (right - left)
    projectionMatrix.m21 = (top + bottom) / (top - bottom)
    projectionMatrix.m22 = - (farPlane + nearPlane) / (farPlane - nearPlane)
    projectionMatrix.m23 = -1
    projectionMatrix.m32 = - (2 * farPlane * nearPlane) / (farPlane - nearPlane)
    projectionMatrix.m33 = 0

    val viewMatrix = new Matrix4f()
    val modelMatrix = new Matrix4f()

    val matrices = new Matrices(viewMatrix, modelMatrix, projectionMatrix)

    val modelScale = new Vector3f(0.7f, 0.7f, 0.7f) //new Vector3f(0.5f/(SIZE-2), 0.5f/(SIZE-2), 0.5f/(SIZE-2))
    val modelPos = new Vector3f(0,0,0)//Vector3f(-(SIZE-1)/2, -(SIZE-1)/2, 0)
    val modelRotate = new Vector3f(10, -10, 0)
    val camera = new Camera()
    val model = new Model(modelPos, modelScale, modelRotate)

    val tmogs = new Transmogrifiers(camera, model)
    (tmogs, matrices)
  }
}
