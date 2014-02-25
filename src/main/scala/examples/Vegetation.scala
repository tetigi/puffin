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
import com.puffin.context.Context
import com.puffin.character.Entity

object Vegetation {
  // The array containing volume data
  val SIZE = 128
  val volume = new Volume(SIZE, 20, SIZE)
  val WIDTH = 1024
  val HEIGHT = 768

  val opts = new RenderOptions()

  def start() = {
    
    Context.debug = true
    //volume.fillSimplexNoise(1.1)
    volume.fillSmallHills()
    World.putThing(volume)

    opts.setOcclusionEnabled(true)

    // Setup input
    Keyboard.enableRepeatEvents(true)

    // init OpenGL
    setupOpenGL(WIDTH, HEIGHT)
    setupMatrices(WIDTH, HEIGHT) 
    setupShaders("shaders/vert.glsl", "shaders/frag.glsl", "shaders/geom.glsl")
    World.entity.moveToCell(0, 20, -40)
    World.entity.lookLng(20.toRadians)


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
      World.entity.update(1.0f/60f)
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
    World.renderWorld(opts)
  }

  def logicCycle() = {
    //tmogs.cameraPos.z -= 0.05f
    //World.model.rotation.y += 1.0f/12f
    //tmogs.model.position.z += 0.01f
    Entity.controlCycle(World.entity)
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

