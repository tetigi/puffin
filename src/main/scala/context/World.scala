package com.puffin.context

import scala.collection.mutable.ListBuffer

import com.puffin.Common.RenderOptions
import com.puffin.objects.SimpleObject
import com.puffin.data.Array3D
import com.puffin.context.BlockType.BlockType

object World {
  var size = (100, 100, 100)
  val blocks = Array3D.initWith(size._1, size._2, size._3, { () => new Block(BlockType.AIR) })

  val things = new ListBuffer[SimpleObject]()

  def putThing(thing: SimpleObject) {
    things += thing
    for (point <- thing.getUsedPoints) {
      val block = blocks.get(point.x + 50, point.y + 50, point.z  + 50)
      block.blockType = BlockType.GROUND
      block.setObjectRef(thing)
      blocks.put(point.x + 50, point.y + 50, point.z + 50, block)
    }
  }

  def getRelative(thing: SimpleObject, x: Int, y: Int, z: Int): Block = {
    val p = thing.getPosition
    val (rx, ry, rz) = (p.x + x, p.y + y, p.z + z)  
    //TODO Should probably check for OOB
    blocks.get(rx + 50, ry + 50, rz + 50)
  }

  def getOccupiedRelative(thing: SimpleObject, x: Int, y: Int, z: Int): Boolean =
    getRelative(thing, x, y, z).blockType != BlockType.AIR

  def putObject(x: Int, y: Int, z: Int, obj: SimpleObject) {
    things += obj
    for (point <- obj.getUsedPoints) {
      val block = blocks.get(point.x + 50, point.y + 50, point.z + 50)
      block.blockType = BlockType.GROUND
      block.setObjectRef(obj)
      blocks.put(point.x + 50, point.y + 50, point.z + 50, block)
    }
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

}

class Block(var blockType: BlockType) {
  def this(bType: BlockType, obj: SimpleObject) = {
    this(bType)
    objectRef = Some(obj)
  }
  var objectRef: Option[SimpleObject] = None
  def setObjectRef(obj: SimpleObject) {
    if (obj != null)
      objectRef = Some(obj)
  }
}

object BlockType extends Enumeration {
  type BlockType = Value
  val AIR, GROUND = Value
}
