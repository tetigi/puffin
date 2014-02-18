package com.puffin.utils

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
import com.puffin.utils._
import com.puffin.context._

object ShaderUtils {
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
    Context.programId = GL20.glCreateProgram()
    GL20.glAttachShader(Context.programId, vertShader)
    //GL20.glAttachShader(Context.programId, geomShader)
    GL20.glAttachShader(Context.programId, fragShader)

    GL20.glLinkProgram(Context.programId)
    GL20.glValidateProgram(Context.programId)

    val pvmLoc = GL20.glGetUniformLocation(Context.programId, "pvm")
    val normalMatrixLoc = GL20.glGetUniformLocation(Context.programId, "normalMatrix")
    val diffuseLoc = GL20.glGetUniformLocation(Context.programId, "diffuse")
    val lDirLoc = GL20.glGetUniformLocation(Context.programId, "l_dir")
    val ambientLoc = GL20.glGetUniformLocation(Context.programId, "ambient")
    GL41.glProgramUniform4f(Context.programId, ambientLoc, 0.2f, 0.2f, 0.2f, 1.0f)
    GL41.glProgramUniform4f(Context.programId, diffuseLoc, 0.2f, 0.2f, 0.2f, 1.0f)

    /*
    val lightDir = new Vector4f(-1, 1, 1, 0)
    Matrix4f.transform(matrices.viewMatrix, lightDir, lightDir)
    lightDir.normalise(lightDir)
    val lightDirBuffer = BufferUtils.createFloatBuffer(4)
    lightDir.store(lightDirBuffer)
    lightDirBuffer.flip()

    GL41.glProgramUniform3(program, lDirLoc, lightDirBuffer)
    */

    GL20.glUseProgram(Context.programId)
    uniformLocs = new UniformLocations(pvmLoc, normalMatrixLoc)
  }

  def storeMatrices(matrices: Matrices, tmogs: Transmogrifiers) = {
    val cam = tmogs.camera
    val model = tmogs.model
    //reset the view and model matrices
    
    model.putModelMatrix(matrices.modelMatrix)
    cam.putViewMatrix(matrices.viewMatrix)
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
