package com.puffin.context

import scala.collection.mutable.ListBuffer
import scala.collection.mutable.LinkedList
import scala.math.round

import com.puffin.Common._
import com.puffin.objects.SimpleObject
import com.puffin.data.Map3D
import com.puffin.data.Set3D
import com.puffin.context.BlockType.BlockType
import com.puffin.utils._
import com.puffin.character.Entity

object World {
  val air = new Block(BlockType.AIR)
  val size = (200, 200, 200)
  val blocks = Map3D.mapWithDefault({ () => air})
  val occupied = new Set3D()

  var model: Model = null
  var entity: Entity = null

  val things = new ListBuffer[SimpleObject]()

  //def getData = blocks.map({ b: Block => if (b.blockType == BlockType.AIR) 0 else 1 })
  @inline
  def cam2cell(x: Float, y: Float, z: Float) = (round(x*size._1), round(y*size._2), round(z*size._3))
  @inline
  def cell2cam(x: Int, y: Int, z: Int) = (x.toFloat/size._1, y.toFloat/size._1, z.toFloat/size._1)

  val halfBlock: Float = 1f/(2*size._1)

  def putThing(thing: SimpleObject) {
    things += thing
    for (point <- thing.getUsedPoints) {
      val block = new Block(BlockType.GROUND)
      block.setObjectRef(thing)
      put(point.x, point.y, point.z, block)
    }
  }

  def get(x: Int, y: Int, z: Int): Block = blocks.get(x, y, z)
  def put(x: Int, y: Int, z: Int, b: Block) {
    blocks.put(x, y, z, b)
    if (b.blockType != BlockType.AIR) 
      occupied.add(x, y, z)
  }

  def getEntityCell() = cam2cell(entity.pos.x, entity.pos.y, entity.pos.z)

  def getRelative(thing: SimpleObject, x: Int, y: Int, z: Int): Block = {
    val p = thing.getPosition
    val (rx, ry, rz) = (p.x + x, p.y + y, p.z + z)  
    //TODO Should probably check for OOB
    get(rx, ry, rz)
  }

  def getOccupiedRelative(thing: SimpleObject, x: Int, y: Int, z: Int): Boolean = {
    val p = thing.getPosition
    val (rx, ry, rz) = (p.x + x, p.y + y, p.z + z)  
    getOccupied(rx, ry, rz)
  }

  def getOccupiedRelative(p: Point3, x: Int, y: Int, z: Int): Boolean = {
    val (rx, ry, rz) = (p.x + x, p.y + y, p.z + z)  
    getOccupied(rx, ry, rz)
  }
  
  def getOccupied(x: Int, y: Int, z: Int): Boolean = {
    occupied.contains(x, y, z)//||
      //(x, y, z) == getEntityCell()
  }

  def renderWorld() {
    things.map(_.render(Context.opts))
  }

  def tickWorld() {
    things.clone().map(_.tick())
  }

  def occludeBlocks(ps: Iterable[Point3]) {
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
