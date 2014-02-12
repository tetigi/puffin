package com.puffin.examples

import java.nio.FloatBuffer

import org.lwjgl.BufferUtils
import org.lwjgl.opengl.Display
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL15
import org.lwjgl.opengl.GL20
import org.lwjgl.opengl.GL30
import org.lwjgl.util.vector.Matrix4f
import org.lwjgl.util.vector.Vector3f
import org.lwjgl.util.glu.GLU
import org.lwjgl.opengl.ARBFragmentShader
import org.lwjgl.opengl.ARBVertexShader
import org.lwjgl.opengl.ARBGeometryShader4
import org.lwjgl.opengl.ARBShaderObjects  

import scala.math._

import com.puffin.objects.Volume
import com.puffin.Common._
import com.puffin.shaders.ShaderUtils._
import com.puffin.shaders.UniformLocations
import com.puffin.shaders.Matrices
import com.puffin.shaders.Transmogrifiers
import com.puffin.render.GLUtils._
import com.puffin.render.RawQuads
import com.puffin.render.QuadUtils._

object Terrain {
  // The array containing volume data
  val SIZE = 5
  val volume = new Volume(SIZE)
  val WIDTH = 800
  val HEIGHT = 600

  var matrix44Buffer: FloatBuffer = null
  var matrices: Matrices = null
  var tmogs: Transmogrifiers = null

  def setupMatrices() = {
    val projectionMatrix = new Matrix4f()
    val fieldOfView = 50f
    val aspectRatio = WIDTH.toFloat / HEIGHT.toFloat
    val nearPlane = 0.1f
    val farPlane = 100f

    def cotan(x: Double) = 1.0 / tan(x)
    val yScale = cotan(toRadians((fieldOfView / 2f)).toDouble).toFloat
    val xScale = yScale / aspectRatio
    val frustumLength = farPlane - nearPlane
    
    projectionMatrix.m00 = xScale
    projectionMatrix.m11 = yScale
    projectionMatrix.m22 = -((farPlane + nearPlane) / frustumLength)
    projectionMatrix.m23 = -1
    projectionMatrix.m23 = -((2 * nearPlane * farPlane) / frustumLength)
    projectionMatrix.m32 = 0

    val viewMatrix = new Matrix4f()
    val modelMatrix = new Matrix4f()

    matrices = new Matrices(viewMatrix, modelMatrix, projectionMatrix)

    val cameraPos = new Vector3f(0.1f, 0.4f , -1)
    val modelScale = new Vector3f(0.5f/(SIZE-2), 0.5f/(SIZE-2), 0.5f/(SIZE-2))
    val modelPos = new Vector3f(-(SIZE-1)/2, -(SIZE-1)/2, 0)
    
    tmogs = new Transmogrifiers(cameraPos, modelScale, modelPos)

    matrix44Buffer = BufferUtils.createFloatBuffer(16)
  }

  def start() = {
    
    volume.fillRandom(0.2)

    // init OpenGL
    setupOpenGL(WIDTH, HEIGHT)
    setupMatrices() 
    setupShaders("shaders/vert.glsl", "shaders/frag.glsl")

    // Start rendering
    // Set all the matrices and shaders
    runShaders(matrices, tmogs)
    storeMatrices(matrix44Buffer, matrices)

    // Get quads and render them
    val quads = volume.getRawQuads()
    renderQuads(quads)

    // Update the changes
    Display.update()

    while (! Display.isCloseRequested()) {
      // do nothing
    }

    // Finish
    Display.destroy()
    println("Done!")
  }

  def main(args : Array[String]) = {
    Terrain.start()
  }
}

