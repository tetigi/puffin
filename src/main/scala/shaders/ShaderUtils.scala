package com.puffin.shaders

import java.nio.FloatBuffer

import org.lwjgl.opengl.ARBShaderObjects
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL20
import org.lwjgl.opengl.GL32
import org.lwjgl.opengl.GL41
import org.lwjgl.util.vector.Matrix4f
import org.lwjgl.util.vector.Vector3f
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
    val projectionMatrixLoc = GL20.glGetUniformLocation(program, "projectionMatrix")
    val viewMatrixLoc = GL20.glGetUniformLocation(program, "viewMatrix")
    val modelMatrixLoc = GL20.glGetUniformLocation(program, "modelMatrix")
    val colorLoc = GL20.glGetUniformLocation(program, "color");
    GL41.glProgramUniform4f(program, colorLoc, 1, 0, 0, 1);

    //reset the view and model matrices
    matrices.viewMatrix.setIdentity()
    matrices.modelMatrix.setIdentity()
    Matrix4f.translate(tmogs.cameraPos, matrices.viewMatrix, matrices.viewMatrix)

    Matrix4f.scale(tmogs.modelScale, matrices.modelMatrix, matrices.modelMatrix)
    Matrix4f.translate(tmogs.modelPos, matrices.modelMatrix, matrices.modelMatrix)

    val rotateX: Double = tmogs.modelRotate.x.toDouble
    val rotateY: Double = tmogs.modelRotate.y.toDouble
    Matrix4f.rotate(toRadians(rotateX).toFloat, new Vector3f(1, 0, 0), matrices.modelMatrix, matrices.modelMatrix)
    Matrix4f.rotate(toRadians(rotateY).toFloat, new Vector3f(0, 1, 0), matrices.modelMatrix, matrices.modelMatrix)

    GL20.glUseProgram(program)
    uniformLocs = new UniformLocations(projectionMatrixLoc, viewMatrixLoc, modelMatrixLoc)
  }

  def storeMatrices(matrix44Buffer: FloatBuffer, matrices: Matrices) = {
    matrices.projectionMatrix.store(matrix44Buffer)
    matrix44Buffer.flip()
    GL20.glUniformMatrix4(uniformLocs.projectionMatrixLoc, false, matrix44Buffer)
    matrices.viewMatrix.store(matrix44Buffer)
    matrix44Buffer.flip()
    GL20.glUniformMatrix4(uniformLocs.viewMatrixLoc, false, matrix44Buffer)
    matrices.modelMatrix.store(matrix44Buffer)
    matrix44Buffer.flip()
    GL20.glUniformMatrix4(uniformLocs.modelMatrixLoc, false, matrix44Buffer)
  }
}

class UniformLocations(val projectionMatrixLoc: Int, val viewMatrixLoc: Int, val modelMatrixLoc: Int) {
}

class Matrices(val viewMatrix: Matrix4f, val modelMatrix: Matrix4f, val projectionMatrix: Matrix4f) {
}

class Transmogrifiers(val cameraPos: Vector3f, val modelScale: Vector3f, val modelPos: Vector3f, val modelRotate: Vector3f) {
}
