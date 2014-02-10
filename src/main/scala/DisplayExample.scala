import org.lwjgl.LWJGLException
import org.lwjgl.opengl.Display
import org.lwjgl.opengl.DisplayMode

object DisplayExample {
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

    while (! Display.isCloseRequested()) {
      
      // render OpenGL here

      Display.update()
    }

    Display.destroy()
    println("Done!")
  }

  //def main(args : Array[String]) = {
  //  println(System.getProperty("java.library.path"))
  //  DisplayExample.start()
  //}

}
