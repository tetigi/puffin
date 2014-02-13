package com.puffin.shaders

import java.nio.FloatBuffer

import org.lwjgl.BufferUtils
import org.lwjgl.opengl.ARBShaderObjects
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL20
import org.lwjgl.opengl.GL32
import org.lwjgl.opengl.GL41
import org.lwjgl.util.vector.Matrix3f
import org.lwjgl.util.vector.Matrix4f
import org.lwjgl.util.vector.Vector3f
import org.lwjgl.util.vector.Vector4f
import scala.math._

import com.puffin.Common.readFileAsString

object ShaderUtils {
  var program = 0
  var uniformLocs: UniformLocations = null

  def createShader(filename: String, shaderType: Int): Int   = {
    def getLogInfo(obj: Int) =
      GL20.glGetShaderInfoLog(obj, 1000).trim()
    var shader = 0
    try {
      shader = GL20.glCreateShader(shaderType)
      if (shader == 0) return 0

      GL20.glShaderSource(shader, readFileAsString(filename))
      GL20.glCompileShader(shader)

      if (GL20.glGetShaderi(shader, GL20.GL_COMPILE_STATUS) == GL11.GL_FALSE) {
        throw new RuntimeException("Error creating shader: " + getLogInfo(shader))
      }
      return shader
    } catch {
      case ex: Exception => {
        println("Something went wrong while getting shader " + filename)
        println(getLogInfo(shader))
        throw ex
      }
    }
  }

  def setupShaders(vShaderFilename: String, fShaderFilename: String, gShaderFilename: String) = {
    val vertShader = createShader(vShaderFilename, GL20.GL_VERTEX_SHADER)
    //val geomShader = createShader(gShaderFilename, GL32.GL_GEOMETRY_SHADER)
    val fragShader = createShader(fShaderFilename, GL20.GL_FRAGMENT_SHADER)
    program = GL20.glCreateProgram()
    GL20.glAttachShader(program, vertShader)
    //GL20.glAttachShader(program, geomShader)
    GL20.glAttachShader(program, fragShader)

    //GL20.glBindAttribLocation(program, 0, "normal")
    //GL20.glBindAttribLocation(program, 1, "position")
    //GL20.glBindAttribLocation(program, 2, "color")

    GL20.glLinkProgram(program)
    GL20.glValidateProgram(program)
  }

  def runShaders(matrices: Matrices, tmogs: Transmogrifiers) = {
    val pvmLoc = GL20.glGetUniformLocation(program, "pvm")
    val normalMatrixLoc = GL20.glGetUniformLocation(program, "normalMatrix")
    val diffuseLoc = GL20.glGetUniformLocation(program, "diffuse")
    val lDirLoc = GL20.glGetUniformLocation(program, "l_dir")
    val ambientLoc = GL20.glGetUniformLocation(program, "ambient")
    GL41.glProgramUniform4f(program, ambientLoc, 0.2f, 0.2f, 0.2f, 1.0f)
    GL41.glProgramUniform4f(program, diffuseLoc, 0.2f, 0.2f, 0.2f, 1.0f)

    //reset the view and model matrices
    matrices.viewMatrix.setIdentity()
    matrices.modelMatrix.setIdentity()
    Matrix4f.translate(tmogs.cameraPos, matrices.viewMatrix, matrices.viewMatrix)

    Matrix4f.scale(tmogs.modelScale, matrices.modelMatrix, matrices.modelMatrix)
    Matrix4f.translate(tmogs.modelPos, matrices.modelMatrix, matrices.modelMatrix)

    val lightDir = new Vector4f(-1, 1, 1, 0)
    Matrix4f.transform(matrices.viewMatrix, lightDir, lightDir)
    lightDir.normalise(lightDir)
    val lightDirBuffer = BufferUtils.createFloatBuffer(4)
    lightDir.store(lightDirBuffer)
    lightDirBuffer.flip()

    GL41.glProgramUniform3(program, lDirLoc, lightDirBuffer)

    val rotateX: Double = tmogs.modelRotate.x.toDouble
    val rotateY: Double = tmogs.modelRotate.y.toDouble
    Matrix4f.rotate(toRadians(rotateX).toFloat, new Vector3f(1, 0, 0), matrices.modelMatrix, matrices.modelMatrix)
    Matrix4f.rotate(toRadians(rotateY).toFloat, new Vector3f(0, 1, 0), matrices.modelMatrix, matrices.modelMatrix)

    GL20.glUseProgram(program)
    uniformLocs = new UniformLocations(pvmLoc, normalMatrixLoc)
  }

  def storeMatrices(matrices: Matrices) = {
    val matrix33Buffer = BufferUtils.createFloatBuffer(9)
    val matrix44Buffer = BufferUtils.createFloatBuffer(16)

    val pvm = new Matrix4f()
    Matrix4f.mul(matrices.projectionMatrix, matrices.viewMatrix, pvm)
    Matrix4f.mul(pvm, matrices.modelMatrix, pvm)
    pvm.store(matrix44Buffer)
    matrix44Buffer.flip()
    GL20.glUniformMatrix4(uniformLocs.pvmLoc, false, matrix44Buffer)

    val mv = new Matrix4f()
    Matrix4f.mul(matrices.modelMatrix, matrices.viewMatrix, mv)
    // Get upper left 3x3
    val mv3 = new Matrix3f()
    mv3.m00 = mv.m00; mv3.m01 = mv.m01; mv3.m02 = mv.m02
    mv3.m10 = mv.m10; mv3.m11 = mv.m11; mv3.m12 = mv.m12
    mv3.m20 = mv.m20; mv3.m21 = mv.m21; mv3.m22 = mv.m22
    mv3.invert()
    mv3.transpose()
    mv3.store(matrix33Buffer)
    matrix33Buffer.flip()    
    GL20.glUniformMatrix3(uniformLocs.normalMatrixLoc, false, matrix33Buffer)

  }
}

class UniformLocations(val pvmLoc: Int, val normalMatrixLoc: Int) {
}

class Matrices(val viewMatrix: Matrix4f, val modelMatrix: Matrix4f, val projectionMatrix: Matrix4f) {
}

class Transmogrifiers(val cameraPos: Vector3f, val modelScale: Vector3f, val modelPos: Vector3f, val modelRotate: Vector3f) {
}
