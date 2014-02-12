package com.puffin.render

import java.nio.FloatBuffer
import java.nio.IntBuffer

import org.lwjgl.BufferUtils
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL15
import org.lwjgl.opengl.GL20
import org.lwjgl.opengl.GL30

object QuadUtils {
  def renderQuads(quads: RawQuads) = {
    println("Rendering quads. There are " + quads.verts.length + " vertices.")
    val vertBuffer = BufferUtils.createFloatBuffer(quads.verts.length)
    vertBuffer.put(quads.verts)
    vertBuffer.flip()
    val indices = generateIndices(quads.verts.length).map(_.toShort).toArray
    val indicesBuffer = BufferUtils.createShortBuffer(indices.length)
    indicesBuffer.put(indices.toArray)
    indicesBuffer.flip()

    // SETUP QUAD
    val vaoId = GL30.glGenVertexArrays()
    GL30.glBindVertexArray(vaoId)

    val vboId = GL15.glGenBuffers()
    GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboId)
    GL15.glBufferData(GL15.GL_ARRAY_BUFFER, vertBuffer, GL15.GL_STATIC_DRAW)

    // Place these in the attributes for the shader
    GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 0, 0)
    GL20.glEnableVertexAttribArray(0)

    val vboiId = GL15.glGenBuffers()
    GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, vboiId)
    GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, indicesBuffer, GL15.GL_STATIC_DRAW)
      
    GL11.glDrawElements(GL11.GL_TRIANGLES, indices.length, GL11.GL_UNSIGNED_SHORT, 0)
    GL30.glBindVertexArray(0)
  }

  def generateIndices(n: Int, start: Int = 0): List[Int] =
    if (n == 0) Nil
    else start :: (start + 1) :: (start + 2) :: (start + 2) :: (start + 3) :: start :: generateIndices(n - 6, start + 4)
}

class RawQuads (val verts: Array[Float], val normals: Array[Float]) {
}
