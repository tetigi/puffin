package com.puffin.render

import java.nio.FloatBuffer
import java.nio.IntBuffer

import org.lwjgl.BufferUtils
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL15
import org.lwjgl.opengl.GL20
import org.lwjgl.opengl.GL30

object QuadUtils {
  var vaoId, vboId, vbonId, vboOccId, vboiId = 0

  def initBuffIds() = {
    vaoId = GL30.glGenVertexArrays()
    vboId = GL15.glGenBuffers()
    vbonId = GL15.glGenBuffers()
    vboOccId = GL15.glGenBuffers()
    vboiId = GL15.glGenBuffers()
  }
  
  def renderQuads(quads: RawQuads) = {
    // SETUP QUAD

    GL30.glBindVertexArray(vaoId)
    GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboId)
    GL15.glBufferData(GL15.GL_ARRAY_BUFFER, quads.vertBuffer, GL15.GL_STATIC_DRAW)

    // Place these in the attributes for the shader
    GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 0, 0)
    GL20.glEnableVertexAttribArray(0)

    GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbonId)
    GL15.glBufferData(GL15.GL_ARRAY_BUFFER, quads.normalBuffer, GL15.GL_STATIC_DRAW)
    GL20.glVertexAttribPointer(1, 3, GL11.GL_FLOAT, false, 0, 0)
    GL20.glEnableVertexAttribArray(1)

    GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboOccId)
    GL15.glBufferData(GL15.GL_ARRAY_BUFFER, quads.occlusionBuffer, GL15.GL_STATIC_DRAW)
    GL20.glVertexAttribPointer(2, 1, GL11.GL_FLOAT, false, 0, 0)
    GL20.glEnableVertexAttribArray(2)

    GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, vboiId)
    GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, quads.indicesBuffer, GL15.GL_STATIC_DRAW)
      
    GL11.glDrawElements(GL11.GL_TRIANGLES, quads.indices.length, GL11.GL_UNSIGNED_INT, 0)
    GL30.glBindVertexArray(0)
  }

  def generateIndices(n: Int, start: Int = 0, indices: List[Int] = Nil): List[Int] =
    if (n == 0) indices.reverse
    else generateIndices(n - 6, start + 4, start :: (start + 1) :: (start + 2) :: (start + 2) :: (start + 3) :: start :: indices)

}

class RawQuads (val verts: Array[Float], val normals: Array[Float], val occlusion: Array[Float]) {
  val vertBuffer = BufferUtils.createFloatBuffer(verts.length)
  vertBuffer.put(verts)
  vertBuffer.flip()
  val normalBuffer = BufferUtils.createFloatBuffer(normals.length)
  normalBuffer.put(normals)
  normalBuffer.flip()
  val indices = QuadUtils.generateIndices(verts.length / 2)
  val indicesBuffer = BufferUtils.createIntBuffer(indices.length)
  indicesBuffer.put(indices.toArray)
  indicesBuffer.flip()
  val occlusionBuffer = BufferUtils.createFloatBuffer(occlusion.length)
  occlusionBuffer.put(occlusion)
  occlusionBuffer.flip()

}
