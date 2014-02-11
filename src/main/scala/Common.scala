import org.lwjgl.opengl.ARBShaderObjects
import org.lwjgl.opengl.GL11

import scala.math._

object Common {
  def clamp(x: Int, ulim: Int) = 
    min(ulim, x)

  def clamp(x: Int, llim: Int, ulim: Int) = 
    max(llim, min(ulim, x))

  def clamp(x: Double, llim: Double, ulim: Double) = 
    max(llim, min(ulim, x))

  def createShader(filename: String, shaderType: Int): Int   = {
    def getLogInfo(obj: Int) =
      ARBShaderObjects.glGetInfoLogARB(obj, ARBShaderObjects.glGetObjectParameteriARB(obj, ARBShaderObjects.GL_OBJECT_INFO_LOG_LENGTH_ARB))
    var shader = 0
    try {
      shader = ARBShaderObjects.glCreateShaderObjectARB(shaderType)

      if (shader == 0) return 0

      ARBShaderObjects.glShaderSourceARB(shader, readFileAsString(filename))
      ARBShaderObjects.glCompileShaderARB(shader)

      if (ARBShaderObjects.glGetObjectParameteriARB(shader, ARBShaderObjects.GL_OBJECT_COMPILE_STATUS_ARB) == GL11.GL_FALSE)
        throw new RuntimeException("Error creating shader: " + getLogInfo(shader))

      return shader
    } catch {
      case ex: Exception => {
        ARBShaderObjects.glDeleteObjectARB(shader)
        throw ex
      }
    }
  }

  def readFileAsString(filename: String) = {
    val source = scala.io.Source.fromFile(filename)
    val lines = source.mkString
    source.close()
    lines
  }
}
