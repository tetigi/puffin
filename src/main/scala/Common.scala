import org.lwjgl.opengl.ARBShaderObjects
import org.lwjgl.opengl.GL20
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

  def readFileAsString(filename: String) = {
    val source = scala.io.Source.fromFile(filename)
    val lines = source.mkString
    source.close()
    lines
  }
}
