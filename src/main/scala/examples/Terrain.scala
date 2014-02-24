package com.puffin.examples

import org.lwjgl.input._
import org.lwjgl.opengl.Display
import org.lwjgl.opengl.GL11

import scala.math._

import com.puffin.objects.Volume
import com.puffin.Common._
import com.puffin.utils.ShaderUtils._
import com.puffin.utils._
import com.puffin.context.World

object Terrain {
  // The array containing volume data
  val SIZE = 40
  val volume = new Volume(SIZE)
  val WIDTH = 1024
  val HEIGHT = 768

  def start() = {
    
    //volume.fillRandom(0.5)
    volume.fillSimplexNoise(1.1)
    //volume.fillFloatingRock()
    //volume.fillIsland()

    // Setup input
    Keyboard.enableRepeatEvents(true)

    // init OpenGL
    setupOpenGL(WIDTH, HEIGHT)
    setupMatrices(WIDTH, HEIGHT) 
    setupShaders("shaders/vert.glsl", "shaders/frag.glsl", "shaders/geom.glsl")

    while (! Display.isCloseRequested()) {
      loopCycle()
      Display.sync(60)
      Display.update()
    }

    // Finish
    destroyOpenGL()
    println("Done!")
  }

  def renderCycle() = {
    GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT)
    // Set all the matrices
    storeMatrices()
    // Get quads and render them
    volume.render()
  }

  def logicCycle() = {
    //tmogs.entityPos.z -= 0.05f
    World.model.rotation.y += 1.0f/12f
    //tmogs.model.position.z += 0.01f
    val posDelta = 0.1f
    while (Keyboard.next()) {
      if (Keyboard.getEventKeyState()) {
        Keyboard.getEventKey() match {
          case Keyboard.KEY_W => World.entity.moveForward(posDelta)
          case Keyboard.KEY_A => World.entity.moveLateral(-posDelta)
          case Keyboard.KEY_S => World.entity.moveForward(-posDelta)
          case Keyboard.KEY_D => World.entity.moveLateral(posDelta)

          case Keyboard.KEY_LEFT => World.entity.lookLat(toRadiansF(-5))
          case Keyboard.KEY_RIGHT => World.entity.lookLat(toRadiansF(5))
          case Keyboard.KEY_UP => World.entity.lookLng(toRadiansF(5))
          case Keyboard.KEY_DOWN => World.entity.lookLng(toRadiansF(-5))
          case _ => ()
        }
      }
    }
    if (Mouse.isButtonDown(0)) {
      World.entity.lookLat(toRadiansF(Mouse.getDX()/6f))
      World.entity.lookLng(toRadiansF(-Mouse.getDY()/6f))
    }
  }

  def loopCycle() = {
    // Update logic
    logicCycle()
    // Update rendered frame
    renderCycle()
  }
  /*
  def main(args : Array[String]) = {
    Terrain.start()
  }
  */
}

