package com.puffin.objects

import com.puffin.Common._
import com.puffin.data.Array3D

class Plane(width: Int, length: Int) extends SimpleObject {
  val (dimX, dimY, dimZ) = (width, 1, length)
  val data = new Array3D[Int](dimX, dimY, dimZ)
  var position = new Point3(-width/2, -1, -length/2)

  def getData = data
  def getUsedPoints = for (x <- 0 until width; z <- 0 until length) yield new Point3(x, 0, z) + position
  def getDims = (dimX, dimY, dimZ)
  def getPosition = position

  def tick {}
  
  // Fill the plane
  for (x <- 0 until width; z <- 0 until length)
    data.put(x, 0, z, 1)
}
