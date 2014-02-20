package com.puffin.context

import scala.collection.mutable.ListBuffer

import com.puffin.Common.RenderOptions
import com.puffin.objects.SimpleObject

object World {
  val things = new ListBuffer[SimpleObject]()

  def putThing(thing: SimpleObject) = things += thing

  def getRelative(x: Int, y: Int, z: Int): PlaceHolder = {
    new PlaceHolder()
  }

  def getOccupiedRelative(x: Int, y: Int, z: Int): Boolean = false
    //getRelative(x, y, z).blockType != BlockType.AIR

  def putObjectRelative(x: Int, y: Int, z: Int, obj: SimpleObject) {
    things += obj
  }

  def renderWorld() {
    things.map(_.render())
  }
  def renderWorld(opts: RenderOptions) {
    things.map(_.render(opts))
  }

  def tickWorld() {
    things.clone().map(_.tick())
  }

  class PlaceHolder {
    val blockType = BlockType.GROUND
    //println("World.Placeholder: I need to be implemented!")
  }


}

object BlockType extends Enumeration {
  type BlockType = Value
  val AIR, GROUND, TREE = Value
}
