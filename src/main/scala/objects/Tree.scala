package com.puffin.objects

import com.puffin.data.Array3D

import com.puffin.render.Quads

class Tree extends Quads {
  val (dimX, dimY, dimZ) = (3, 10, 3)
  val data = new Array3D[Int](dimX, dimY, dimZ)

  def getData = data
  def getDims = (dimX, dimY, dimZ)
}
