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
  
  val maxWalkSpeed = 1f // cubes per second?
  val maxFallSpeed = -0.1f
  val gravity = new Vector3f(0, -9.8f, 0)
  val faccel = new Vector3f()
  val laccel = new Vector3f()
  val velocity = new Vector3f()
  val feet = 4
  val fat = 1

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
    scaleVector3f(faccel, maxWalkSpeed, faccel)
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
    scaleVector3f(faccel, maxWalkSpeed, faccel)
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
    val scaledAccel = new Vector3f()
    scaleVector3f(faccel, step, scaledAccel)
    Vector3f.add(scaleVector3f(laccel, step, tmp), scaledAccel, scaledAccel)
    if (!noclip) { // add gravity too
      Vector3f.add(scaledAccel, scaleVector3f(gravity, step, tmp), scaledAccel)
    }
    // update current vel to get new vel
    Vector3f.add(velocity, scaledAccel, velocity)
    var speed = sqrt(velocity.x*velocity.x + velocity.z*velocity.z).toFloat
    if (speed > maxWalkSpeed) {
      scaleVector3f(velocity, (maxWalkSpeed*maxWalkSpeed)/(speed*speed), velocity)
    }
    speed = velocity.y
    if (speed < maxFallSpeed) {
      velocity.y = maxFallSpeed
    }
    val scaledVel = new Vector3f()
    scaleVector3f(velocity, step, scaledVel)
    val newPos = new Vector3f()
    Vector3f.add(pos, scaledVel, newPos)
    if (!noclip) {
      // Do gravity check
      var (cx, cy, cz) = World.cam2cell(newPos.x, newPos.y, newPos.z)
      println("new pos is " + (cx, cy, cz))
      if (World.getOccupied(cx, cy - feet, cz)) {
        println("Hit something!")
        val (_, newY, _) = World.cell2cam(cx, cy, cz)
        // project to cell.f + 0.5
        newPos.y = newY
        velocity.y = 0
        println("So new pos is " + World.cam2cell(newPos.x, newPos.y, newPos.z))
      }
      // Do side check
      if (World.getOccupied(cx, cy, cz)) {
        val (newX, _, newZ) = World.cell2cam(cx, cy, cz)
        newPos.x = pos.x
        newPos.z = pos.z
      }

    }
    pos.set(newPos)
  }
}
