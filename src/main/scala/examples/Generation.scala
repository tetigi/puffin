package com.puffin.examples

import org.lwjgl.input._
import org.lwjgl.opengl.Display
import org.lwjgl.opengl.GL11

import scala.math._

import com.puffin.utils.generation.VolumeGenerator
import com.puffin.objects.Volume
import com.puffin.Common._
import com.puffin.utils.ShaderUtils._
import com.puffin.utils._
import com.puffin.context.World
import com.puffin.context.Context
import com.puffin.character.Entity

object Generation {
  // The array containing volume data
  val SIZE = 164
  val WIDTH = 1024
  val HEIGHT = 768

  val opts = new RenderOptions()

  def start() = {
    
    Context.debug = true

    //val volume = VolumeGenerator.genVolumeFromBitmap("resources/face.bmp")
    val volume = VolumeGenerator.genVolumeFromBitmap("resources/thing.bmp")
    World.putThing(volume)

    opts.setOcclusionEnabled(true)

    // Setup input
    Keyboard.enableRepeatEvents(true)

    // init OpenGL
    setupOpenGL(WIDTH, HEIGHT)
    setupMatrices(WIDTH, HEIGHT) 
    setupShaders("shaders/vert.glsl", "shaders/frag.glsl", "shaders/geom.glsl")
    World.entity.moveToCell(0, 10, 20)
    World.entity.lookLat(180.toRadians)
    World.entity.lookLng(20.toRadians)



    var ticker = 0
    while (! Display.isCloseRequested()) {
      loopCycle()
      Display.sync(60)
      Display.update()
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
    World.model.rotation.y += 1.0f/12f
    Entity.controlCycle(World.entity)
  }

  def loopCycle() = {
    // Update logic
    logicCycle()
    // Update rendered frame
    renderCycle()
  }

  def main(args : Array[String]) = {
    Generation.start()
  }
}

