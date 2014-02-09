import org.lwjgl.LWJGLException
import org.lwjgl.opengl.Display
import org.lwjgl.opengl.DisplayMode
import org.lwjgl.opengl.GL11

object QuadExample {
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
    QuadExample.start()
  }

}
