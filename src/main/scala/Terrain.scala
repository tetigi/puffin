import java.nio.FloatBuffer

import org.lwjgl.BufferUtils
import org.lwjgl.LWJGLException
import org.lwjgl.opengl.Display
import org.lwjgl.opengl.DisplayMode
import org.lwjgl.opengl.ContextAttribs
import org.lwjgl.opengl.PixelFormat
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL15
import org.lwjgl.opengl.GL20
import org.lwjgl.opengl.GL30
import org.lwjgl.util.vector.Matrix4f
import org.lwjgl.util.glu.GLU
import org.lwjgl.opengl.ARBFragmentShader
import org.lwjgl.opengl.ARBVertexShader
import org.lwjgl.opengl.ARBGeometryShader4
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
    val ns: ListBuffer[(Int,Int,Int)] = new ListBuffer()
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
    val quads: ListBuffer[FloatBuffer] = new ListBuffer()
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
            var verts: Array[Float] = null
            if (dx != 0) { 
              //GL11.glNormal3d(dx * 2, 0, 0)
              verts = Array(
                nx + dx, ny + d, nz - d,
                nx + dx, ny + d, nz + d,
                nx + dx, ny - d, nz + d,
                nx + dx, ny - d, nz - d)
            } else if (dy != 0) {
              //GL11.glNormal3d(0, dy * 2, 0)
              verts = Array(
                nx + d, ny + dy, nz - d,
                nx + d, ny + dy, nz + d,
                nx - d, ny + dy, nz + d,
                nx - d, ny + dy, nz - d)
            } else if (dz != 0) {
              //GL11.glNormal3d(0, 0, dz * 2)
              verts = Array(
                nx + d, ny - d, nz + dz,
                nx + d, ny + d, nz + dz,
                nx - d, ny + d, nz + dz,
                nx - d, ny - d, nz + dz)
            }

            val vertBuffer = BufferUtils.createFloatBuffer(verts.length)
            vertBuffer.put(verts)
            vertBuffer.flip()
            quads += vertBuffer
          }
        }
    }
    val verts = Array(
      -0.5f, 0.5f, 0f,    // Left top         ID: 0
      -0.5f, -0.5f, 0f,   // Left bottom      ID: 1
      0.5f, -0.5f, 0f,    // Right bottom     ID: 2
      0.5f, 0.5f, 0f      // Right left       ID: 3
      )
    val vertBuffer = BufferUtils.createFloatBuffer(verts.length)
    vertBuffer.put(verts)
    vertBuffer.flip()
    //quads
    ListBuffer(vertBuffer)
  }
}

object Terrain {
  // The array containing volume data
  val volume = new Volume(20)
  val WIDTH = 800
  val HEIGHT = 600

  def setupOpenGL() = {
    try {
      val context = new ContextAttribs(3,2).withProfileCore(true).withForwardCompatible(true)
      Display.setDisplayMode(new DisplayMode(WIDTH, HEIGHT))
      Display.create(new PixelFormat(), context)

    } catch {
      case ex: LWJGLException => {
        ex.printStackTrace()
        System.exit(0)
      }
    }

    GL11.glClearColor(0.4f, 0.6f, 0.9f, 0f)
    GL11.glViewport(0, 0, WIDTH, HEIGHT)
  }
  
  def start() = {
    
    volume.fillRandom(0.2)


    // init OpenGL here
    setupOpenGL()
    
    val vertShader = createShader("shaders/vert.glsl", GL20.GL_VERTEX_SHADER)
    //val geomShader = createShader("shaders/geom.glsl", ARBGeometryShader4.GL_GEOMETRY_SHADER_ARB)
    val fragShader = createShader("shaders/frag.glsl", GL20.GL_FRAGMENT_SHADER)
    val program = GL20.glCreateProgram()
    GL20.glAttachShader(program, vertShader)
    //ARBShaderObjects.glAttachObjectARB(program, geomShader)
    GL20.glAttachShader(program, fragShader)

    //GL20.glBindAttribLocation(program, 0, "normal")
    //GL20.glBindAttribLocation(program, 1, "position")
    //GL20.glBindAttribLocation(program, 2, "color")

    GL20.glLinkProgram(program)
    GL20.glValidateProgram(program)
    GL20.glUseProgram(program)

    val verts = Array(
      -0.5f, 0.5f, 0f,    // Left top         ID: 0
      -0.5f, -0.5f, 0f,   // Left bottom      ID: 1
      0.5f, -0.5f, 0f,    // Right bottom     ID: 2
      0.5f, 0.5f, 0f      // Right left       ID: 3
      )
    val vertBuffer = BufferUtils.createFloatBuffer(verts.length)
    vertBuffer.put(verts)
    vertBuffer.flip()
      
    //val quads = QuadGen.tesselateQuads(volume)
    val indices: Array[Byte] = Array(0, 1, 2, 2, 3, 0)
    val indicesCount = indices.length
    val indicesBuffer = BufferUtils.createByteBuffer(indicesCount)
    indicesBuffer.put(indices)
    indicesBuffer.flip()

    //for (quadBuffer <- quads) {
    println("quad")

    // SETUP QUAD
    val vaoId = GL30.glGenVertexArrays()
    GL30.glBindVertexArray(vaoId)

    val vboId = GL15.glGenBuffers()
    GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboId)
    GL15.glBufferData(GL15.GL_ARRAY_BUFFER, vertBuffer, GL15.GL_STATIC_DRAW)
    GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 0, 0)
    GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0)

    GL30.glBindVertexArray(0)

    val vboiId = GL15.glGenBuffers()
    GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, vboiId)
    GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, indicesBuffer, GL15.GL_STATIC_DRAW)
    GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0)
      
    // LOOP CYCLE
    GL11.glClear(GL11.GL_COLOR_BUFFER_BIT)
    GL30.glBindVertexArray(vaoId)
    GL20.glEnableVertexAttribArray(0)
    GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, vboiId)

    GL11.glDrawElements(GL11.GL_TRIANGLES, indicesCount, GL11.GL_UNSIGNED_BYTE, 0)

    GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0)
    GL20.glDisableVertexAttribArray(0)
    GL30.glBindVertexArray(0)
   // }
      
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
