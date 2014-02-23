package com.puffin.objects

import com.puffin.render.Quads
import com.puffin.data.Array3D
import com.puffin.Common._

class MiniChunk(val data: Array3D[Int], val pos: Point) extends Quads {
  val dims = (data.dimX, data.dimY, data.dimZ)
  def getData = data
  def getUsedPoints = for ((x, y, z) <- xyzIn(0, data.dimX, data.dimY, data.dimZ)) yield (pos + new Point(x, y, z))
  def getDims = dims
  def getPosition = pos
}
