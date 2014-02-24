package com.puffin.utils

import org.lwjgl.util.vector.Vector2f
import org.lwjgl.util.vector.Vector3f
import org.lwjgl.util.vector.Matrix3f
import org.lwjgl.util.vector.Matrix4f
import scala.math._

import com.puffin.Common._
import com.puffin.context.World

// Camera class assumes that up is always y
class Camera (val pos: Vector3f, val dir: Vector3f) {
  val up = new Vector3f(0, 1, 0)
  private val tmp = new Vector3f()

  def this() = this(new Vector3f(0, 0, -1), new Vector3f(0, 0, 1))

  def getRight() = Vector3f.cross(up, dir, tmp).normalise(tmp)
  
  def moveBy(dv: Vector3f) =
    Vector3f.add(pos, dv, pos)

  def moveTo(v: Vector3f) =
    pos.set(v)

  def moveForward(d: Float) =
    Vector3f.add(pos, scaleVector3f(dir, d, tmp), pos)

  def moveLateral(d: Float) = 
    Vector3f.add(pos, scaleVector3f(getRight(), -d, tmp), pos)

  def lookAt(v: Vector3f) = {
    Vector3f.sub(v, pos, dir)
    dir.normalise(dir)
  }

  var lng = 0f
  // Look lng and look lat functions look around the unit sphere around the pos
  def lookLng(phi: Float) = {
    // TODO Don't let look up further than top or bottom
    // Rotate around y axis to bring into x/y plane
    // Get theta to rotate
    val theta = -atan2(dir.z, dir.x).toFloat
    rotateY(dir, theta)
    val curPhi = atan2(dir.x, dir.y)
    // Rotate around z to change dir
    if (curPhi - phi > 0 && curPhi - phi < Pi)
      rotateZ(dir, phi)
      lng -= phi
    rotateY(dir, -theta)
  }
  var lat = 0f

  def lookLat(phi: Float) = {
    rotateY(dir, -phi)
    lat -= phi
  }

  def putViewMatrix(dest: Matrix4f) = {
    val defaultDir = new Vector3f(0, 0, 1)

    dest.setIdentity()

    dest.rotate(lng, new Vector3f(1, 0, 0))
    dest.rotate(lat, new Vector3f(0, 1, 0))
    // scale pos to cam coords
    //val scaledPos = new Vector3f()
    //scaledPos.set(pos.x / World.size._1, pos.y / World.size._2, pos.z / World.size._3)
    dest.translate(pos) // Move to cam position
    dest
  }
}
