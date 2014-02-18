package com.puffin.render

import java.nio.FloatBuffer
import java.nio.IntBuffer

import org.lwjgl.BufferUtils
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL15
import org.lwjgl.opengl.GL20
import org.lwjgl.opengl.GL30

import com.puffin.context._
import com.puffin.render._
import com.puffin.Common._

trait Quads extends RenderableBase {
  var rawQuadCache: RawQuadData = null

  def createRawQuadData(opts: RenderOptions) : RawQuadData

  def render(opts: RenderOptions) = {
    if (rawQuadCache == null) 
      rawQuadCache = createRawQuadData(opts)
    renderQuads(rawQuadCache)
  }

  private def renderQuads(quads: RawQuadData) = {
    // SETUP QUAD

    GL30.glBindVertexArray(Context.vaoId)
    GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, Context.vboVertexId)
    GL15.glBufferData(GL15.GL_ARRAY_BUFFER, quads.vertBuffer, GL15.GL_STATIC_DRAW)
    GL20.glVertexAttribPointer(Context.vertexAttribArray, 3, GL11.GL_FLOAT, false, 0, 0)
    // Place these in the attributes for the shader

    GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, Context.vboNormalId)
    GL15.glBufferData(GL15.GL_ARRAY_BUFFER, quads.normalBuffer, GL15.GL_STATIC_DRAW)
    GL20.glVertexAttribPointer(Context.normalAttribArray, 3, GL11.GL_FLOAT, false, 0, 0)
    GL20.glEnableVertexAttribArray(Context.vertexAttribArray)

    GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, Context.vboOcclusionId)
    GL15.glBufferData(GL15.GL_ARRAY_BUFFER, quads.occlusionBuffer, GL15.GL_STATIC_DRAW)
    GL20.glVertexAttribPointer(Context.occlusionAttribArray, 1, GL11.GL_FLOAT, false, 0, 0)
    GL20.glEnableVertexAttribArray(Context.normalAttribArray)

    GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, Context.vboIndicesId)
    GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, quads.indicesBuffer, GL15.GL_STATIC_DRAW)
    GL20.glEnableVertexAttribArray(Context.occlusionAttribArray)
      
    GL11.glDrawElements(GL11.GL_TRIANGLES, quads.indices.length, GL11.GL_UNSIGNED_INT, 0)
    GL30.glBindVertexArray(0)
  }


  class RawQuadData (val verts: Array[Float], val normals: Array[Float], val occlusion: Array[Float]) {
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
}

object QuadUtils {
  def generateIndices(n: Int, start: Int = 0, indices: List[Int] = Nil): List[Int] =
    if (n == 0) indices.reverse
    else generateIndices(n - 6, start + 4, start :: (start + 1) :: (start + 2) :: (start + 2) :: (start + 3) :: start :: indices)
}
