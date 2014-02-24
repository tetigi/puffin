package com.puffin.character

import org.lwjgl.util.vector.Vector3f
import org.lwjgl.input.Keyboard
import org.lwjgl.input.Mouse

import scala.math._

import com.puffin.Common._
import com.puffin.utils.Camera
import com.puffin.context.World

object Entity {
  def controlCycle(entity: Entity) {
    // Entity should stop if movement keys aren't down
    if (   !Keyboard.isKeyDown(Keyboard.KEY_W)
        && !Keyboard.isKeyDown(Keyboard.KEY_A)
        && !Keyboard.isKeyDown(Keyboard.KEY_S)
        && !Keyboard.isKeyDown(Keyboard.KEY_D)) entity.stop()

    while (Keyboard.next()) {
      if (Keyboard.getEventKeyState()) {
        Keyboard.getEventKey() match {
          case Keyboard.KEY_W => entity.goForwards()
          case Keyboard.KEY_A => entity.goLeft()
          case Keyboard.KEY_S => entity.goBackwards()//moveForward(-posDelta)
          case Keyboard.KEY_D => entity.goRight()//moveLateral(posDelta)
          case Keyboard.KEY_SPACE => entity.toggleNoclip()

          case _ => ()
        }
      }
    }
    if (Mouse.isButtonDown(0)) {
      entity.lookLat(toRadiansF(Mouse.getDX()/6f))
      entity.lookLng(toRadiansF(-Mouse.getDY()/6f))
    }
  }
}

class Entity extends Camera {
  var noclip = true
  // Need a way to provide the current camera matrix to the renderer
  // Inherit moveForward etc. from Camera
  // Which thread should this run in? Main thread? Probably.
  // How do I add physics for character? In an update func?
  // I know the dir and position from the camera (pos, dir)
  
  val maxWalkSpeed = 0.2f // cubes per second?
  val maxFallSpeed = 1f
  val gravity = new Vector3f(0, -9.8f, 0)
  val faccel = new Vector3f()
  val laccel = new Vector3f()
  val velocity = new Vector3f()
  val feet = new Vector3f(0, -2, 0) // feet are 2 blocks below head

  // TODO Need to zero the direction when it's not being pressed independent of others
  def enableNoclip() {
    noclip = true
    laccel.setY(0)
    faccel.setY(0)
    velocity.setY(0)
  }

  def disableNoclip() {
    noclip = false
  }

  def toggleNoclip() {
    if (noclip) disableNoclip()
    else enableNoclip()
  }

  def goForwards() {
    faccel.set(dir.x, dir.y, dir.z)
  }

  def goLeft() {
    val right = getRight()
    laccel.set(-right.x, -right.y, -right.z)
  } 

  def goRight() {
    val right = getRight()
    laccel.set(right.x, right.y, right.z)
  }

  def goBackwards() {
    faccel.set(-dir.x, -dir.y, -dir.z)
  }
  
  def stop() {
    faccel.setX(0); laccel.setX(0)
    if (noclip) {
      faccel.setY(0); laccel.setY(0)
    }
    faccel.setZ(0); laccel.setZ(0)
    velocity.setX(0)
    if (noclip) velocity.setY(0)
    velocity.setZ(0)
  }

  // Called by the master as a way to update yourself
  // step is the number of seconds in a step
  def update(step: Float) {
    val tmp = new Vector3f()
    println(pos)
    val scaledAccel = new Vector3f()
    scaleVector3f(faccel, step, scaledAccel)
    Vector3f.add(scaleVector3f(laccel, step, tmp), scaledAccel, scaledAccel)
    if (!noclip) { // add gravity too
      Vector3f.add(scaledAccel, scaleVector3f(gravity, step, tmp), scaledAccel)
    }
    // update current vel to get new vel
    Vector3f.add(velocity, scaledAccel, velocity)
    val speed = sqrt(velocity.lengthSquared).toFloat
    if (speed > maxWalkSpeed) {
      scaleVector3f(velocity, (maxWalkSpeed*maxWalkSpeed)/(speed*speed), velocity)
    }
    val scaledVel = new Vector3f()
    scaleVector3f(velocity, step, scaledVel)
    Vector3f.add(pos, scaledVel, pos)
  }
}
