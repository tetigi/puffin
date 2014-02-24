package com.puffin.context

import scala.collection.mutable.ListBuffer
import scala.collection.mutable.LinkedList
import scala.math.round

import com.puffin.Common._
import com.puffin.objects.SimpleObject
import com.puffin.data.Array3D
import com.puffin.context.BlockType.BlockType
import com.puffin.render.Quads

object World {
  val size = (100, 100, 100)
  val offset = (size._1 / 2, size._2 / 2, size._3 / 2) 
  val blocks = Array3D.initWith(size._1, size._2, size._3, { () => new Block(BlockType.AIR) })

  val things = new ListBuffer[SimpleObject]()

  //def getData = blocks.map({ b: Block => if (b.blockType == BlockType.AIR) 0 else 1 })
  def cam2cell(x: Float, y: Float, z: Float) = (round(x*size._1), round(y*size._2), round(z*size._3))
  def cell2cam(x: Int, y: Int, z: Int) = (x.toFloat/size._1, y.toFloat/size._1, z.toFloat/size._1)

  def putThing(thing: SimpleObject) {
    things += thing
    for (point <- thing.getUsedPoints) {
      val block = get(point.x, point.y, point.z)
      block.blockType = BlockType.GROUND
      block.setObjectRef(thing)
      put(point.x, point.y, point.z, block)
    }
  }

  def get(x: Int, y: Int, z: Int): Block = blocks.get(x + offset._1, y + offset._2, z + offset._3)
  def put(x: Int, y: Int, z: Int, b: Block) = blocks.put(x + offset._1, y + offset._2, z + offset._3, b)

  def getRelative(thing: SimpleObject, x: Int, y: Int, z: Int): Block = {
    val p = thing.getPosition
    val (rx, ry, rz) = (p.x + x, p.y + y, p.z + z)  
    //TODO Should probably check for OOB
    get(rx, ry, rz)
  }

  def getOccupiedRelative(thing: SimpleObject, x: Int, y: Int, z: Int): Boolean =
    getRelative(thing, x, y, z).blockType != BlockType.AIR
  
  def getOccupiedCamSpace(x: Float, y: Float, z: Float): Boolean = {
    val (cx, cy, cz) = cam2cell(x, y, z)
    getOccupied(cx, cy, cz)
  }
  
  def getOccupied(x: Int, y: Int, z: Int): Boolean =
    get(x, y, z).blockType != BlockType.AIR

  def renderWorld() {
    things.map(_.render())
  }
  def renderWorld(opts: RenderOptions) {
    things.map(_.render(opts))
  }

  def tickWorld() {
    things.clone().map(_.tick())
  }

  def occludeBlocks(ps: Iterable[Point]) {
    for (p <- ps) {
      // TODO Do the occlusion in world-space then
      // assign the new occlusion values to the objects responsible
      // for that block
    }
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
