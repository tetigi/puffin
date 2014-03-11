package com.puffin

import com.puffin.utils.Camera
import com.puffin.character.Entity
import com.puffin.utils.Model
import com.puffin.context._

import org.lwjgl.LWJGLException
import org.lwjgl.util.vector.Matrix4f
import org.lwjgl.util.vector.Vector3f
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL15
import org.lwjgl.opengl.GL20
import org.lwjgl.opengl.GL30
import org.lwjgl.opengl.Display
import org.lwjgl.opengl.DisplayMode
import org.lwjgl.opengl.PixelFormat
import org.lwjgl.opengl.ContextAttribs

import scala.math._

package object utils { 

  class UniformLocations(val vmLoc: Int, val pvmLoc: Int, val normalMatrixLoc: Int, val lightDirLoc: Int) {}

  class Matrices(val viewMatrix: Matrix4f, val modelMatrix: Matrix4f, val projectionMatrix: Matrix4f) {}

  private def initialiseBuffers() = {
    Context.vaoId = GL30.glGenVertexArrays()
    Context.vboVertexId = GL15.glGenBuffers()
    Context.vboNormalId = GL15.glGenBuffers() 
    Context.vboOcclusionId = GL15.glGenBuffers()
    Context.vboIndicesId = GL15.glGenBuffers()
  }

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

    initialiseBuffers()

  }

  def destroyOpenGL() = {
    GL20.glUseProgram(0)
    GL20.glDeleteProgram(Context.programId)

    GL30.glBindVertexArray(Context.vaoId)
    GL20.glDisableVertexAttribArray(0) // Vertex
    GL20.glDisableVertexAttribArray(1) // Normal
    GL20.glDisableVertexAttribArray(2) // Occlusion

    GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0)
    GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0)
    GL15.glDeleteBuffers(Context.vboVertexId)
    GL15.glDeleteBuffers(Context.vboNormalId)
    GL15.glDeleteBuffers(Context.vboOcclusionId)
    GL15.glDeleteBuffers(Context.vboIndicesId)

    GL30.glBindVertexArray(0)
    GL30.glDeleteVertexArrays(Context.vaoId)

    Display.destroy()
  }

  def setupMatrices(width: Int, height: Int) = {
    val projectionMatrix = new Matrix4f()
    val fieldOfView = 80f
    val aspectRatio = width.toFloat / height.toFloat
    val nearPlane = 0.001f
    val farPlane = 5f

    def cotan(x: Double) = 1.0 / tan(x)
    val top = nearPlane * tan((Pi / 180) * fieldOfView / 2f).toFloat
    val bottom = -top
    val right = top * aspectRatio
    val left = -right

    projectionMatrix.m00 = - 2 * nearPlane / (right - left)
    projectionMatrix.m11 = - 2 * nearPlane / (top - bottom)
    projectionMatrix.m20 = - (right + left) / (right - left)
    projectionMatrix.m21 = - (top + bottom) / (top - bottom)
    projectionMatrix.m22 = - (farPlane + nearPlane) / (farPlane - nearPlane)
    projectionMatrix.m23 = -1
    projectionMatrix.m32 = - (2 * farPlane * nearPlane) / (farPlane - nearPlane)
    projectionMatrix.m33 = 0

    val viewMatrix = new Matrix4f()
    val modelMatrix = new Matrix4f()

    val matrices = new Matrices(viewMatrix, modelMatrix, projectionMatrix)

    val modelScale = new Vector3f(-1, -1, -1) 
    val modelPos = new Vector3f(0,0,0)
    val modelRotate = new Vector3f(0, 0, 0)
    val entity = new Entity()
    val model = new Model(modelPos, modelScale, modelRotate)

    World.model = model
    World.entity = entity
    Context.matrices = matrices
  }
}
