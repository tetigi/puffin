package com.puffin.data

import com.puffin.Common.Point

import scala.collection.mutable.HashSet

class Set3D () {
  val data = new HashSet[Point]()
  def add(x: Int, y: Int, z: Int) =
    data += Point(x, y, z)

  def contains(x: Int, y: Int, z: Int) =
    data.contains(Point(x, y, z))
}
