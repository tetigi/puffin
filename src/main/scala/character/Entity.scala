package com.puffin.character

import org.lwjgl.util.vector.Vector3f

import scala.math._

import com.puffin.Common._
import com.puffin.utils.Camera
import com.puffin.context.World

class Entity extends Camera {
  var noclip = true
  // Need a way to provide the current camera matrix to the renderer
  // Inherit moveForward etc. from Camera
  // Which thread should this run in? Main thread? Probably.
  // How do I add physics for character? In an update func?
  // I know the dir and position from the camera (pos, dir)
  
  val maxSpeed = 0.1f // cubes per second?
  val accel = new Vector3f()
  var speed = 0.0f
  def getVelocity = {
    val vel = new Vector3f()
    scaleVector3f(dir, speed, vel)
  }

  def goForwards() {
    println("Going forwards!")
    accel.set(dir.x, dir.y, dir.z)
  }

  def goLeft() {
    val right = getRight()
    accel.set(-right.x, -right.y, -right.z)
  } 

  def goRight() {
    val right = getRight()
    accel.set(right.x, right.y, right.z)
  }

  def goBackwards() {
    accel.set(-dir.x, -dir.y, -dir.z)
  }
  
  def stop() {
    accel.set(0, 0, 0)
    speed = 0.0f
  }

  // Called by the master as a way to update yourself
  // step is the number of seconds in a step
  def update(step: Float) {
    if (noclip) {
      // Need to check for collisions here
      val currentVel = getVelocity
      val scaledAccel = new Vector3f()
      scaleVector3f(accel, step, scaledAccel)
      // update current vel to get new vel
      val newVel = new Vector3f() // TODO make this work
      Vector3f.add(currentVel, scaledAccel, newVel)
      val scaledNewVel = new Vector3f()
      scaleVector3f(newVel, step, scaledNewVel)
      speed = min(maxSpeed/step, sqrt(newVel.lengthSquared).toFloat)
      moveForward(speed)
    }
  }
}
