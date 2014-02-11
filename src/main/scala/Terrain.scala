import org.lwjgl.LWJGLException
import org.lwjgl.opengl.Display
import org.lwjgl.opengl.DisplayMode
import org.lwjgl.opengl.GL11
import org.lwjgl.util.vector.Matrix4f
import org.lwjgl.util.glu.GLU
import org.lwjgl.opengl.ARBFragmentShader
import org.lwjgl.opengl.ARBVertexShader
import org.lwjgl.opengl.ARBShaderObjects  

import scala.math._
import scala.collection.mutable.ListBuffer

import Common._

class Volume(val size: Int) {
  val data = new Array[Int](size*size*size)
  
  def get(x: Int, y: Int, z: Int) =
    data(clamp(x, size-1) + clamp(y, size-1)*size + clamp(z, size-1)*size*size)

  def put(x: Int, y: Int, z: Int, value: Int) =
    data(clamp(x, size-1) + clamp(y, size-1)*size + clamp(z, size-1)*size*size) = value

  def fillRandom(p: Double = 0.5) = {
    //Pick random cells to fill
    val fill = 
      (for {
        x <- 1 until size -1
        y <- 1 until size -1
        z <- 1 until size -1
      } yield (x, y, z, random)) filter { _._4 <= clamp(p, 0, 1) }
    fill map { x => put(x._1, x._2, x._3, 1) }
    ()
  }

  // Gets adjacent neighbours
  def getNeighbours(x: Int, y: Int, z: Int) = {
    var ns: ListBuffer[(Int,Int,Int)] = new ListBuffer()
    if (x > 0) ns += ((x - 1, y, z))
    if (y > 0) ns += ((x, y - 1, z))
    if (z > 0) ns += ((x, y, z - 1))

    if (x < size - 1) ns += ((x + 1, y, z))
    if (y < size - 1) ns += ((x, y + 1, z))
    if (z < size - 1) ns += ((x, y, z + 1))
    ns
  }
}

object QuadGen {
  def tesselateQuads(vol: Volume) = {
    for {
      x <- 0 until vol.size
      y <- 0 until vol.size
      z <- 0 until vol.size
      } {
        if (vol.get(x,y,z) == 0) {
          for ((nx,ny,nz) <- vol.getNeighbours(x,y,z) if vol.get(nx,ny,nz) != 0) {
            // Generate quad
            val d = 0.5f
            val dx: Float = (x.toFloat - nx) / 2.0f
            val dy: Float = (y.toFloat - ny) / 2.0f
            val dz: Float = (z.toFloat - nz) / 2.0f
            
            GL11.glBegin(GL11.GL_QUADS)
            if (dx != 0) { 
              GL11.glVertex3f(nx + dx, ny + d, nz - d)
              GL11.glVertex3f(nx + dx, ny + d, nz + d)
              GL11.glVertex3f(nx + dx, ny - d, nz + d)
              GL11.glVertex3f(nx + dx, ny - d, nz - d)
            } else if (dy != 0) {
              GL11.glVertex3f(nx + d, ny + dy, nz - d)
              GL11.glVertex3f(nx + d, ny + dy, nz + d)
              GL11.glVertex3f(nx - d, ny + dy, nz + d)
              GL11.glVertex3f(nx - d, ny + dy, nz - d)
            } else if (dz != 0) {
              GL11.glVertex3f(nx + d, ny - d, nz + dz)
              GL11.glVertex3f(nx + d, ny + d, nz + dz)
              GL11.glVertex3f(nx - d, ny + d, nz + dz)
              GL11.glVertex3f(nx - d, ny - d, nz + dz)
            }
            GL11.glEnd()
          }
        }
    }
  }
}

object Terrain {
  // The array containing volume data
  val volume = new Volume(20)
  val WIDTH = 800
  val HEIGHT = 600

  def start() = {
    try {
      Display.setDisplayMode(new DisplayMode(WIDTH, HEIGHT))
      Display.create()
    } catch {
      case ex: LWJGLException => {
        ex.printStackTrace()
        System.exit(0)
      }
    }
    
    volume.fillRandom(0.2)

    // init OpenGL here
    val vertShader = createShader("shaders/vert.glsl", ARBVertexShader.GL_VERTEX_SHADER_ARB)
    val fragShader = createShader("shaders/frag.glsl", ARBFragmentShader.GL_FRAGMENT_SHADER_ARB)
    val program = ARBShaderObjects.glCreateProgramObjectARB()
    ARBShaderObjects.glAttachObjectARB(program, vertShader)
    ARBShaderObjects.glAttachObjectARB(program, fragShader)

    ARBShaderObjects.glLinkProgramARB(program)
    ARBShaderObjects.glValidateProgramARB(program)
    ARBShaderObjects.glUseProgramObjectARB(program)

    GL11.glMatrixMode(GL11.GL_PROJECTION)
    GL11.glLoadIdentity()
    GLU.gluPerspective(50, WIDTH.toFloat / HEIGHT.toFloat, 0, 100)
    
    GL11.glMatrixMode(GL11.GL_MODELVIEW)
    GL11.glLoadIdentity()
    GLU.gluLookAt(30, 30, 30, 0, 0, 0, 0, 1, 0)
    GL11.glClearColor(0.8f, 0.8f, 0.8f, 1.0f)

    // Clear the screen and depth buffer
    GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT)
      
    QuadGen.tesselateQuads(volume)
      
    Display.update()
    while (! Display.isCloseRequested()) {
      // do nothing
    }

    Display.destroy()
    println("Done!")
  }

  def main(args : Array[String]) = {
    Terrain.start()
  }

}
