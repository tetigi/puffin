package com.puffin.utils

import org.lwjgl.util.vector.Vector3f
import org.lwjgl.util.vector.Matrix4f

import com.puffin.Common._  

class Model(val position: Vector3f, val scale: Vector3f, val rotation: Vector3f) {
  def putModelMatrix(model: Matrix4f) = {
    model.setIdentity()

    Matrix4f.scale(scale, model, model)
    Matrix4f.translate(position, model, model)

    val rotateX: Double = rotation.x.toDouble
    val rotateY: Double = rotation.y.toDouble
    val rotateZ: Double = rotation.z.toDouble
    Matrix4f.rotate(toRadiansF(rotateX), new Vector3f(1, 0, 0), model, model)
    Matrix4f.rotate(toRadiansF(rotateY), new Vector3f(0, 1, 0), model, model)
    Matrix4f.rotate(toRadiansF(rotateZ), new Vector3f(0, 0, 1), model, model)
    model
  }
}
