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
  
  val maxWalkSpeed = 0.1f // cubes per second?
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

  def moveToCell(x: Int, y: Int, z: Int) {
    val (px, py, pz) = World.cell2cam(x, y, z)
    moveTo(px, py, pz)
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
    
    // work out the 'walking' speed in the xz plane
    var speed = sqrt(velocity.x*velocity.x + velocity.z*velocity.z).toFloat

    // cap the speed as needed
    if (speed > maxWalkSpeed) {
      scaleVector3f(velocity, (maxWalkSpeed*maxWalkSpeed)/(speed*speed), velocity)
    }

    // cap falling speed
    speed = velocity.y
    if (speed < maxFallSpeed) {
      velocity.y = maxFallSpeed
    }

    // work out the new scaled position according to timestep
    val scaledVel = new Vector3f()
    scaleVector3f(velocity, step, scaledVel)

    // work out the new position
    val newPos = new Vector3f()
    Vector3f.add(pos, scaledVel, newPos)

    if (!noclip) {
      // Get new cell in the world
      var (cx, cy, cz) = World.cam2cell(newPos.x, newPos.y, newPos.z)

      // Check for foot collision with that cell (assuming foot is 2 blocks tall)
      if (World.getOccupied(cx, cy - feet, cz) || World.getOccupied(cx, cy - (feet - 1), cz)) {
        //println("Hit something!")

        // Get the coord of that cell
        val (_, newY, _) = if (World.getOccupied(cx, cy - (feet - 1), cz)) World.cell2cam(cx, cy + 1, cz) else World.cell2cam(cx, cy, cz)

        // project to cell
        newPos.y = newY + World.halfBlock

        // Reset downward velocity
        velocity.y = 0
        val scaledAccel = new Vector3f()
        scaleVector3f(faccel, step, scaledAccel)
        Vector3f.add(scaleVector3f(laccel, step, tmp), scaledAccel, scaledAccel)
        
        // Update velocity
        Vector3f.add(scaledAccel, velocity, velocity)
      } else {
        // Nothing was hit, so can keep accelerating downwards
        val scaledAccel = new Vector3f()
        scaleVector3f(faccel, step, scaledAccel)
        Vector3f.add(scaleVector3f(laccel, step, tmp), scaledAccel, scaledAccel)
        Vector3f.add(scaleVector3f(gravity, step, tmp), scaledAccel, scaledAccel)
        
        // Update velocity
        Vector3f.add(scaledAccel, velocity, velocity)
      }
    } else {
        val scaledAccel = new Vector3f()
        scaleVector3f(faccel, step, scaledAccel)
        Vector3f.add(scaleVector3f(laccel, step, tmp), scaledAccel, scaledAccel)
        
        // Update velocity
        Vector3f.add(scaledAccel, velocity, velocity)
    }
    pos.set(newPos)
  }
}
