package com.puffin.objects

import com.puffin.Common._
import com.puffin.data.Array3D

class Plane(width: Int, length: Int) extends SimpleObject {
  val (dimX, dimY, dimZ) = (width + 2, 3, length + 2)
  val data = new Array3D[Int](dimX, dimY, dimZ)
  var position = new Point(-width/2, -2, -length/2)

  def getData = data
  def getDims = (dimX, dimY, dimZ)
  def getPosition = position

  def tick {}
  
  // Fill the plane
  for (x <- 1 to width; z <- 1 to width)
    data.put(x, 1, z, 1)
}
