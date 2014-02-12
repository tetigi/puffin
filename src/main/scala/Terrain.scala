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
import org.lwjgl.util.vector.Vector3f
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
  def getQuadsAndIndices(vol: Volume) = {
    val quadVerts: ListBuffer[Float] = new ListBuffer()
    val indices: ListBuffer[Int] = new ListBuffer()
    var i: Int = 0
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
            if (dx != 0) { 
              //GL11.glNormal3d(dx * 2, 0, 0)
              quadVerts.appendAll(List(
                nx + dx, ny + d, nz - d,
                nx + dx, ny + d, nz + d,
                nx + dx, ny - d, nz + d,
                nx + dx, ny - d, nz - d))
            } else if (dy != 0) {
              //GL11.glNormal3d(0, dy * 2, 0)
              quadVerts.appendAll(List(
                nx + d, ny + dy, nz - d,
                nx + d, ny + dy, nz + d,
                nx - d, ny + dy, nz + d,
                nx - d, ny + dy, nz - d))
            } else if (dz != 0) {
              //GL11.glNormal3d(0, 0, dz * 2)
              quadVerts.appendAll(List(
                nx + d, ny - d, nz + dz,
                nx + d, ny + d, nz + dz,
                nx - d, ny + d, nz + dz,
                nx - d, ny - d, nz + dz))
            }

            indices.appendAll(List(
              i, i+1, i+2,
              i+2, i+3, i))
            i += 4
          }
        }
    }
    (quadVerts.toArray, indices.map(_.toByte).toArray)
  }
}

object Terrain {
  // The array containing volume data
  val SIZE = 5
  val volume = new Volume(SIZE)
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

    GL11.glClearColor(0.8f, 0.8f, 0.8f, 1.0f)
    GL11.glViewport(0, 0, WIDTH, HEIGHT)
  }

  var projectionMatrix: Matrix4f = null
  var viewMatrix: Matrix4f = null
  var modelMatrix: Matrix4f = null
  var matrix44Buffer: FloatBuffer = null

  def setupMatrices() = {
    projectionMatrix = new Matrix4f()
    val fieldOfView = 50f
    val aspectRatio = WIDTH.toFloat / HEIGHT.toFloat
    val nearPlane = 0.1f
    val farPlane = 100f

    def cotan(x: Double) = 1.0 / tan(x)
    val yScale = cotan(toRadians((fieldOfView / 2f)).toDouble).toFloat
    val xScale = yScale / aspectRatio
    val frustumLength = farPlane - nearPlane
    
    projectionMatrix.m00 = xScale
    projectionMatrix.m11 = yScale
    projectionMatrix.m22 = -((farPlane + nearPlane) / frustumLength)
    projectionMatrix.m23 = -1
    projectionMatrix.m23 = -((2 * nearPlane * farPlane) / frustumLength)
    projectionMatrix.m32 = 0

    viewMatrix = new Matrix4f()
    modelMatrix = new Matrix4f()

    matrix44Buffer = BufferUtils.createFloatBuffer(16)
  }

  val cameraPos: Vector3f = new Vector3f(0.1f, 0.4f , -1)
  val modelScale: Vector3f = new Vector3f(0.5f/(SIZE-2), 0.5f/(SIZE-2), 0.5f/(SIZE-2))
  val modelPos: Vector3f = new Vector3f(-(SIZE-1)/2, -(SIZE-1)/2, 0)
  
  def start() = {
    
    volume.fillRandom(0.2)


    // init OpenGL here
    setupOpenGL()
    setupMatrices()
    
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

    val projectionMatrixLoc = GL20.glGetUniformLocation(program, "projectionMatrix")
    val viewMatrixLoc = GL20.glGetUniformLocation(program, "viewMatrix")
    val modelMatrixLoc = GL20.glGetUniformLocation(program, "modelMatrix")

    Matrix4f.translate(cameraPos, viewMatrix, viewMatrix)

    Matrix4f.scale(modelScale, modelMatrix, modelMatrix)
    Matrix4f.translate(modelPos, modelMatrix, modelMatrix)
    Matrix4f.rotate(toRadians(45).toFloat, new Vector3f(1, 0, 0), modelMatrix, modelMatrix)
    Matrix4f.rotate(toRadians(-20).toFloat, new Vector3f(0, 1, 0), modelMatrix, modelMatrix)

    GL20.glUseProgram(program)

    projectionMatrix.store(matrix44Buffer)
    matrix44Buffer.flip()
    GL20.glUniformMatrix4(projectionMatrixLoc, false, matrix44Buffer)
    viewMatrix.store(matrix44Buffer)
    matrix44Buffer.flip()
    GL20.glUniformMatrix4(viewMatrixLoc, false, matrix44Buffer)
    modelMatrix.store(matrix44Buffer)
    matrix44Buffer.flip()
    GL20.glUniformMatrix4(modelMatrixLoc, false, matrix44Buffer)

    /* 
    var verts = Array(
      -0.5f, 0.5f, 0f,    // Left top         ID: 0
      -0.5f, -0.5f, 0f,   // Left bottom      ID: 1
      0.5f, -0.5f, 0f,    // Right bottom     ID: 2
      0.5f, 0.5f, 0f,      // Right left       ID: 3
      0.6f, 0.5f, 0f,    // Left top         ID: 0
      0.6f, -0.5f, 0f,   // Left bottom      ID: 1
      1.1f, -0.5f, 0f,    // Right bottom     ID: 2
      1.1f, 0.5f, 0f      // Right top       ID: 3
      )
    val indices: Array[Byte] = Array(0, 1, 2, 2, 3, 0, 4, 5, 6, 6, 7, 4)
    val indicesCount = indices.length
    */ 
      
    val (verts, indices) = QuadGen.getQuadsAndIndices(volume)
    val vertBuffer = BufferUtils.createFloatBuffer(verts.length)
    vertBuffer.put(verts)
    vertBuffer.flip()
    val indicesBuffer = BufferUtils.createByteBuffer(indices.length)
    indicesBuffer.put(indices.toArray)
    indicesBuffer.flip()

    // SETUP QUAD
    val vaoId = GL30.glGenVertexArrays()
    GL30.glBindVertexArray(vaoId)

    val vboId = GL15.glGenBuffers()
    GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboId)
    GL15.glBufferData(GL15.GL_ARRAY_BUFFER, vertBuffer, GL15.GL_STATIC_DRAW)
    GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 0, 0)
    GL20.glEnableVertexAttribArray(0)

    val vboiId = GL15.glGenBuffers()
    GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, vboiId)
    GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, indicesBuffer, GL15.GL_STATIC_DRAW)
    GL30.glBindVertexArray(0)
      
    GL11.glClear(GL11.GL_COLOR_BUFFER_BIT)
    GL30.glBindVertexArray(vaoId)

    GL11.glDrawElements(GL11.GL_TRIANGLES, indices.length, GL11.GL_UNSIGNED_BYTE, 0)

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
