import org.lwjgl.LWJGLException
import org.lwjgl.opengl.Display
import org.lwjgl.opengl.DisplayMode
import org.lwjgl.opengl.GL11

import scala.math.random

import Common._

class Volume(val size: Int) {
  val data = new Array[Int](size*size*size)
  
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
      } yield (x, y, z, random)) filter { _._4 >= clamp(p, 0, 1) }
    fill map { x => put(x._1, x._2, x._3, 1) }
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
          for (neighbour <- vol.getNeighbours(x,y,z)) {
            // Generate quad

          }
        }
    }
  }
}

object Terrain {
  // The array containing volume data
  val volume = new Volume(20)
  def start() = {
    try {
      Display.setDisplayMode(new DisplayMode(800,600))
      Display.create()
    } catch {
      case ex: LWJGLException => {
        ex.printStackTrace()
        System.exit(0)
      }
    }
    
    // init OpenGL here
    GL11.glMatrixMode(GL11.GL_PROJECTION)
    GL11.glLoadIdentity()
    GL11.glOrtho(0, 800, 0, 600, 1, -1)
    GL11.glMatrixMode(GL11.GL_MODELVIEW)
    GL11.glClearColor(0.8f, 0.8f, 0.8f, 1.0f)

    while (! Display.isCloseRequested()) {
      // Clear the screen and depth buffer
      GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT)

      // Set the color of the quad (RGBA)
      GL11.glColor3f(0.5f, 0.5f, 1.0f)

      // Draw quad
      GL11.glBegin(GL11.GL_QUADS)
      GL11.glVertex2f(100,100)
      GL11.glVertex2f(300,100)
      GL11.glVertex2f(300,300)
      GL11.glVertex2f(100,300)
      GL11.glEnd()
      
      Display.update()
    }

    Display.destroy()
    println("Done!")
  }

  def main(args : Array[String]) = {
    Terrain.start()
  }

}
