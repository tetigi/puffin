package com.puffin.data

import com.puffin.Common.Point3

import scala.collection.mutable.HashSet

class Set3D () {
  val data = new HashSet[Point3]()
  def add(x: Int, y: Int, z: Int) =
    data += Point3(x, y, z)

  def contains(x: Int, y: Int, z: Int) =
    data.contains(Point3(x, y, z))
}
