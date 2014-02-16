package com.puffin.examples

import java.nio.FloatBuffer

import org.lwjgl.BufferUtils
import org.lwjgl.input._
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
import com.puffin.render.Camera

object Terrain {
  // The array containing volume data
  val SIZE = 60
  val volume = new Volume(SIZE)
  val WIDTH = 1024
  val HEIGHT = 768

  var matrices: Matrices = null
  var tmogs: Transmogrifiers = null

  def setupMatrices() = {
    val projectionMatrix = new Matrix4f()
    val fieldOfView = 80f
    val aspectRatio = WIDTH.toFloat / HEIGHT.toFloat
    val nearPlane = 0.1f
    val farPlane = 100f

    def cotan(x: Double) = 1.0 / tan(x)
    val top = nearPlane * tan((Pi / 180) * fieldOfView / 2f).toFloat
    val bottom = -top
    val right = top * aspectRatio
    val left = -right
    
    projectionMatrix.m00 = 2 * nearPlane / (right - left)
    projectionMatrix.m11 = 2 * nearPlane / (top - bottom)
    projectionMatrix.m20 = (right + left) / (right - left)
    projectionMatrix.m21 = (top + bottom) / (top - bottom)
    projectionMatrix.m22 = - (farPlane + nearPlane) / (farPlane - nearPlane)
    projectionMatrix.m23 = -1
    projectionMatrix.m32 = - (2 * farPlane * nearPlane) / (farPlane - nearPlane)
    projectionMatrix.m33 = 0

    val viewMatrix = new Matrix4f()
    val modelMatrix = new Matrix4f()

    matrices = new Matrices(viewMatrix, modelMatrix, projectionMatrix)

    val modelScale = new Vector3f(0.7f, 0.7f, 0.7f) //new Vector3f(0.5f/(SIZE-2), 0.5f/(SIZE-2), 0.5f/(SIZE-2))
    val modelPos = new Vector3f(0,0,0)//Vector3f(-(SIZE-1)/2, -(SIZE-1)/2, 0)
    val modelRotate = new Vector3f(10, -10, 0)
    val camera = new Camera()
    
    tmogs = new Transmogrifiers(camera, modelScale, modelPos, modelRotate)
  }

  def start() = {
    
    //volume.fillRandom(0.5)
    //volume.fillSimplexNoise(0.2)
    volume.fillFloatingRock()

    // Setup input
    Keyboard.enableRepeatEvents(true)

    // init OpenGL
    setupOpenGL(WIDTH, HEIGHT)
    setupMatrices() 
    setupShaders("shaders/vert.glsl", "shaders/frag.glsl", "shaders/geom.glsl")
    initBuffIds()

    while (! Display.isCloseRequested()) {
      loopCycle()
      Display.sync(60)
      Display.update()
    }

    // Finish
    Display.destroy()
    println("Done!")
  }

  def renderCycle() = {
    GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT)
    // Set all the matrices and shaders
    runShaders(matrices, tmogs)
    storeMatrices(matrices)
    // Get quads and render them
    val quads = volume.getRawQuads()
    renderQuads(quads)
  }

  def logicCycle() = {
    //tmogs.cameraPos.z -= 0.05f
    tmogs.modelRotate.y += 1.0f/12f
    //tmogs.modelPos.z += 0.01f
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
      tmogs.camera.lookLng(toRadiansF(Mouse.getDY()/6f))
    }
  }

  def loopCycle() = {
    // Update logic
    logicCycle()
    // Update rendered frame
    renderCycle()
  }

  def main(args : Array[String]) = {
    Terrain.start()
  }
}

