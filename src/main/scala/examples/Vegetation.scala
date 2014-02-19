package com.puffin.examples

import org.lwjgl.input._
import org.lwjgl.opengl.Display
import org.lwjgl.opengl.GL11

import scala.math._

import com.puffin.objects.Volume
import com.puffin.objects.Tree
import com.puffin.objects.Plane
import com.puffin.Common._
import com.puffin.utils.ShaderUtils._
import com.puffin.utils._

object Vegetation {
  // The array containing volume data
  val SIZE = 40
  val plane = new Plane(64, 64)
  val volume = new Volume(SIZE)
  val tree = new Tree()
  val WIDTH = 1024
  val HEIGHT = 768

  var matrices: Matrices = null
  var tmogs: Transmogrifiers = null


  def start() = {
    
    //volume.fillIsland()
    plane.position = new Point(-32, -2, -32)

    // Setup input
    Keyboard.enableRepeatEvents(true)

    // init OpenGL
    setupOpenGL(WIDTH, HEIGHT)
    var (tmogs, matrices) = setupMatrices(WIDTH, HEIGHT) 
    this.tmogs = tmogs; this.matrices = matrices
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
    storeMatrices(matrices, tmogs)
    // Get quads and render them
    tree.render()
    plane.render()
    //volume.render()
  }

  def logicCycle() = {
    //tmogs.cameraPos.z -= 0.05f
    tmogs.model.rotation.y += 1.0f/12f
    //tmogs.model.position.z += 0.01f
    val posDelta = 0.1f
    while (Keyboard.next()) {
      if (Keyboard.getEventKeyState()) {
        Keyboard.getEventKey() match {
          case Keyboard.KEY_W => tmogs.camera.moveForward(posDelta)
          case Keyboard.KEY_A => tmogs.camera.moveLateral(-posDelta)
          case Keyboard.KEY_S => tmogs.camera.moveForward(-posDelta)
          case Keyboard.KEY_D => tmogs.camera.moveLateral(posDelta)

          case Keyboard.KEY_LEFT => tmogs.camera.lookLat(toRadiansF(-5))
          case Keyboard.KEY_RIGHT => tmogs.camera.lookLat(toRadiansF(5))
          case Keyboard.KEY_UP => tmogs.camera.lookLng(toRadiansF(5))
          case Keyboard.KEY_DOWN => tmogs.camera.lookLng(toRadiansF(-5))
          case _ => ()
        }
      }
    }
    if (Mouse.isButtonDown(0)) {
      tmogs.camera.lookLat(toRadiansF(Mouse.getDX()/6f))
      tmogs.camera.lookLng(toRadiansF(-Mouse.getDY()/6f))
    }
  }

  def loopCycle() = {
    // Update logic
    logicCycle()
    // Update rendered frame
    renderCycle()
  }

  def main(args : Array[String]) = {
    Vegetation.start()
  }
}

