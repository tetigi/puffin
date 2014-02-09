import org.lwjgl.LWJGLException
import org.lwjgl.opengl.Display
import org.lwjgl.opengl.DisplayMode
import org.lwjgl.opengl.GL11
import org.lwjgl.util.vector.Matrix4f

import scala.math.random

import Common._

class Volume(val size: Int) {
  val data = new Array[Int]((size+1)*(size+1)*(size+1))
  
  def get(x: Int, y: Int, z: Int) =
    data(clamp(x, size) + clamp(y, size)*size + clamp(z, size)*size*size)

  def put(x: Int, y: Int, z: Int, value: Int) =
    data(x + y*size + z*size*size) = value

  def fillRandom(p: Double = 0.5) = {
    //Pick random cells to fill
    val fill = 
      (for {
        x <- 0 until size
        y <- 0 until size
        z <- 0 until size
      } yield (x, y, z, random)) filter { _._4 <= clamp(p, 0, 1) }
    fill map { x => put(x._1, x._2, x._3, 1) }
    ()
  }

  def getNeighbours(x: Int, y: Int, z: Int) = {
    for {
      nx <- x -1 until x +1
      ny <- y -1 until y +1
      nz <- z -1 until z +1
      if (nx >= 0 && nx < size &&
          ny >= 0 && ny < size &&
          nz >= 0 && nz < size &&
          (nx != x || ny != y || nz != z)) 
    } yield (nx, ny, nz)
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
            val dx: Float = (int2float(x) - nx) / 2.0f
            val dy: Float = (int2float(y) - ny) / 2.0f
            val dz: Float = (int2float(z) - nz) / 2.0f
            
            GL11.glColor3f(0.2f, 0.2f, 0.2f)
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
  val volume = new Volume(50)
  val WIDTH = 800

  val HEIGHT = 600
  var projectionMatrix: Matrix4f = null

  def setupMatrices() = {
    // Setup projection matrix
    projectionMatrix = new Matrix4f()
    val fov = 60f
//    aspectRation =
  }

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
    GL11.glMatrixMode(GL11.GL_PROJECTION)
    GL11.glLoadIdentity()
    GL11.glOrtho(-WIDTH, WIDTH, -HEIGHT, HEIGHT, 1, -1)
    GL11.glMatrixMode(GL11.GL_MODELVIEW)
    GL11.glClearColor(0.8f, 0.8f, 0.8f, 1.0f)

    while (! Display.isCloseRequested()) {
      // Clear the screen and depth buffer
      GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT)
      
      QuadGen.tesselateQuads(volume)
      
      Display.update()
    }

    Display.destroy()
    println("Done!")
  }

  def main(args : Array[String]) = {
    Terrain.start()
  }

}
