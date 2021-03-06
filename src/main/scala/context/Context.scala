package com.puffin.context

import com.puffin.utils.Matrices
import com.puffin.Common._

object Context {
  var vaoId = 0
  var vboVertexId = 0
  var vboNormalId = 0
  var vboOcclusionId = 0
  var vboIndicesId = 0

  var programId = 0

  val vertexAttribArray = 0
  val normalAttribArray = 1
  val occlusionAttribArray = 2

  var debug = false
  var matrices: Matrices = null

  var opts = new RenderOptions()
}
