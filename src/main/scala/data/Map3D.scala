package com.puffin.data

import scala.collection.mutable.HashMap

object Map3D {
  def mapWithDefault[T: Manifest](constructor: () => T) = {
    new Map3D(constructor)
  }
}

class Map3D[T: Manifest] (val default: () => T) {
  def this() = this(null)
  val data: HashMap[Tuple3[Int, Int, Int], T] = new HashMap()

  def get(x: Int, y: Int, z: Int) = data.getOrElse((x,y,z), default())

  def put(x: Int, y: Int, z: Int, value: T) = data.put((x,y,z), value) 
}
