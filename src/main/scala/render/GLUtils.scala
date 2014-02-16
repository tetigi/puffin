package com.puffin.render

import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.Display
import org.lwjgl.opengl.DisplayMode
import org.lwjgl.opengl.PixelFormat
import org.lwjgl.opengl.ContextAttribs
import org.lwjgl.LWJGLException

object GLUtils {
  def setupOpenGL(WIDTH: Int, HEIGHT: Int) = {
    try {
      val context = new ContextAttribs(3,2).withProfileCore(true).withForwardCompatible(true)
      Display.setDisplayMode(new DisplayMode(WIDTH, HEIGHT))
      Display.create(new PixelFormat(), context)

    } catch {
      case ex: LWJGLException => {
        ex.printStackTrace()
        System.exit(0)
      }
    }

    GL11.glClearColor(0.8f, 0.8f, 0.8f, 1.0f)
    GL11.glViewport(0, 0, WIDTH, HEIGHT)
    GL11.glEnable(GL11.GL_DEPTH_TEST)
  }
}
