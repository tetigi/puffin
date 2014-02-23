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
import com.puffin.context.World

object Vegetation {
  // The array containing volume data
  val SIZE = 64
  val plane = new Plane(64, 64)
  val volume = new Volume(SIZE)
  val tree = new Tree()
  val WIDTH = 1024
  val HEIGHT = 768

  var matrices: Matrices = null
  var tmogs: Transmogrifiers = null

  val opts = new RenderOptions()

  def start() = {
    
    //volume.fillSimplexNoise(1.1)
    //volume.fillFloatingRock()
    //volume.fillIsland()
    World.putThing(plane)
    World.putThing(tree)
    //World.putThing(volume)

    opts.setOcclusionEnabled(false)

    // Setup input
    Keyboard.enableRepeatEvents(true)

    // init OpenGL
    setupOpenGL(WIDTH, HEIGHT)
    var (tmogs, matrices) = setupMatrices(WIDTH, HEIGHT) 
    this.tmogs = tmogs; this.matrices = matrices
    setupShaders("shaders/vert.glsl", "shaders/frag.glsl", "shaders/geom.glsl")

    var ticker = 0
    while (! Display.isCloseRequested()) {
      loopCycle()
      Display.sync(60)
      Display.update()
      ticker += 1
      if (ticker >= 300) {
        ticker = 0
        World.tickWorld()
      }
      tmogs.entity.update(1.0f/60f)
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
    World.renderWorld(opts)
    //tree.render()
    //plane.render()
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
          case Keyboard.KEY_W => tmogs.entity.goForwards()//moveForward(posDelta)
          case Keyboard.KEY_A => tmogs.entity.goLeft()//moveLateral(-posDelta)
          case Keyboard.KEY_S => tmogs.entity.goBackwards()//moveForward(-posDelta)
          case Keyboard.KEY_D => tmogs.entity.goRight()//moveLateral(posDelta)
          case Keyboard.KEY_SPACE => tmogs.entity.stop()

          case _ => ()
        }
      }
    }
    if (Mouse.isButtonDown(0)) {
      tmogs.entity.lookLat(toRadiansF(Mouse.getDX()/6f))
      tmogs.entity.lookLng(toRadiansF(-Mouse.getDY()/6f))
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

