package com.puffin.objects

import com.puffin.data.Array3D
import com.puffin.Common.Point
import com.puffin.render.Quads

class Tree extends SimpleObject {
  var (dimX, dimY, dimZ) = (3, 4, 3)
  val data = new Array3D[Int](dimX, dimY, dimZ)

  def getData = data
  def getDims = (dimX, dimY, dimZ)

  def getPosition = new Point()

  var height = 3

  update()

  def grow = {
    if (height < 10) {
      height += 1
      update()
    }
  }

  def update() = {
    dimY = height + 1
    // Redraw the tree
    data.putAll(0)
    for (i <- 0 until height)
      data.put(1, i, 1, 1)
  }
}
