package com.puffin.utils

import org.lwjgl.input.Keyboard
import org.lwjgl.opengl.Display
import org.lwjgl.opengl.GL11

import com.puffin.Common._
import com.puffin.utils.ShaderUtils._
import com.puffin.utils._
import com.puffin.context.World
import com.puffin.character.Entity

trait PuffinApp {
  // The array containing volume data
  val DEFAULT_SIZE = 164
  val WIDTH = 1024
  val HEIGHT = 768

  val opts = new RenderOptions()
  var rotateOn = true

  def init() {
    opts.setOcclusionEnabled(true)

    // Setup input
    Keyboard.enableRepeatEvents(true)

    // init OpenGL
    setupOpenGL(WIDTH, HEIGHT)
    setupMatrices(WIDTH, HEIGHT) 
    setupShaders("shaders/vert.glsl", "shaders/frag.glsl", "shaders/geom.glsl")
  }
  
  def start() {
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
    if (rotateOn) World.model.rotation.y += 1.0f/12f
    Entity.controlCycle(World.entity)
  }

  def loopCycle() = {
    // Update logic
    logicCycle()
    // Update rendered frame
    renderCycle()
  }

}

