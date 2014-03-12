package com.puffin.objects

import scala.math._
import scala.collection.mutable.ListBuffer
import scala.collection.mutable.HashMap

import com.puffin.data.Array3D
import com.puffin.Common.Point3
import com.puffin.context.World
import com.puffin.context.BlockType
import com.puffin.avro.objects.InflateableSimpleObject

object Tree extends InflateableSimpleObject[Tree] {
  def deflate(obj: Tree): com.puffin.avro.objects.SimpleObject = {
    val metadata = new HashMap[String, String]()
    metadata.put("height", obj.height.toString)

    com.puffin.avro.objects.SimpleObject(
      com.puffin.avro.objects.ObjectType.TREE,
      obj.getPosition,
      obj.getUsedPoints.toSeq,
      metadata.toMap)
  }

  def inflate(obj: com.puffin.avro.objects.SimpleObject): Tree = {
    val data = rebuildData(obj.points)
    val tree = new Tree()
    tree.data.copy(data)
    tree.pos.set(obj.position)
    tree.usedPoint3s ++= obj.points
    tree
  }
}

class Tree(x: Int, y: Int, z: Int) extends SimpleObject {
  def this() = this(0, 0, 0)
  var (dimX, dimY, dimZ) = (1, 3, 1)
  var data = new Array3D[Int](dimX, dimY, dimZ)
  val pos = new Point3(x, y, z)

  def getData = data
  val usedPoint3s: ListBuffer[Point3] = new ListBuffer()
  def getUsedPoints = usedPoint3s map (_ + getPosition)
  def getDims = (dimX, dimY, dimZ)

  def getPosition = pos

  def tick() {
    spread()
  }

  var height = 3

  update()

  def grow = {
    if (height < 10) {
      height += 1
      update()
    }
  }

  def adultTree() {
    dimX = 38
    dimY = 38
    dimZ = 38
    data = new Array3D[Int](dimX, dimY, dimZ)
    usedPoint3s.clear()
    // Fill trunk
    val offset = 16
    for (i <- 0 until 16) {
      // fill out a square for the trunk
      for (j <- offset until (offset + 3)) {
        for (k <- offset until (offset + 3)) {
          data.put(j, i, k, 1)
          usedPoint3s += Point3(j, i, k)
        }
      }
    }
    // Cube for leaves TODO make not shit
    for (i <- 16 until 28) {
      for (j <- offset - 12 to offset + 12) {
        for (k <- offset - 12 to offset + 12) {
          if (random < 0.7) {
            data.put(j, i, k, 1)
            usedPoint3s += Point3(j, i, k)
          }
        }
      }
    }

    // Add some vines
    for (i <- 0 until 3) {
      data.put(offset-3, i, offset+6, 1)
      data.put(offset-3, i, offset-3, 1)
      data.put(offset+3, i, offset+5, 1)
      usedPoint3s += Point3(offset-3, i, offset+6)
      usedPoint3s += Point3(offset-3, i, offset-3)
      usedPoint3s += Point3(offset+3, i, offset+5)
    }
  }

  def update() {
    usedPoint3s.clear()
    dimY = height
    // Redraw the tree
    data = new Array3D[Int](dimX, dimY, dimZ)
    for (i <- 0 until height)
      data.put(0, i, 0, 1)
    for (i <- 0 until height) usedPoint3s += new Point3(0, i, 0)
    requiresRefresh = true
  }

  def spread() = {
    // place seeds in random circle
    var seeds = 4
    val seedProb = 0.2
    for (i <- -2 to -2) {
      // -2X    
      // Check there is ground and isn't occupied
      var seedSpot = new Point3(-2, 0, i)
      var isGround = World.getRelative(this, seedSpot.x, seedSpot.y - 1, seedSpot.z).blockType == BlockType.GROUND
      var isNotOccupied = !World.getOccupiedRelative(this, seedSpot.x, seedSpot.y, seedSpot.z)
      if (isGround && isNotOccupied && seeds > 0 && random < seedProb) {
        World.putThing(new Tree(pos.x + seedSpot.x, pos.y + seedSpot.y, pos.z + seedSpot.z))
        seeds -= 1
      }
      // -2Z    
      // Check there is ground and isn't occupied
      seedSpot = new Point3(2, 0, i)
      isGround = World.getRelative(this, seedSpot.x, seedSpot.y - 1, seedSpot.z).blockType == BlockType.GROUND
      isNotOccupied = !World.getOccupiedRelative(this, seedSpot.x, seedSpot.y, seedSpot.z)
      if (isGround && isNotOccupied && seeds > 0 && random < seedProb) {
        World.putThing(new Tree(pos.x + seedSpot.x, pos.y + seedSpot.y, pos.z + seedSpot.z))
        seeds -= 1
      }
      // +2X    
      // Check there is ground and isn't occupied
      seedSpot = new Point3(-2, 0, -i)
      isGround = World.getRelative(this, seedSpot.x, seedSpot.y - 1, seedSpot.z).blockType == BlockType.GROUND
      isNotOccupied = !World.getOccupiedRelative(this, seedSpot.x, seedSpot.y, seedSpot.z)
      if (isGround && isNotOccupied && seeds > 0 && random < seedProb) {
        World.putThing(new Tree(pos.x + seedSpot.x, pos.y + seedSpot.y, pos.z + seedSpot.z))
        seeds -= 1
      }
      // +2Z    
      // Check there is ground and isn't occupied
      seedSpot = new Point3(2, 0, -i)
      isGround = World.getRelative(this, seedSpot.x, seedSpot.y - 1, seedSpot.z).blockType == BlockType.GROUND
      isNotOccupied = !World.getOccupiedRelative(this, seedSpot.x, seedSpot.y, seedSpot.z)
      if (isGround && isNotOccupied && seeds > 0 && random < seedProb) {
        World.putThing(new Tree(pos.x + seedSpot.x, pos.y + seedSpot.y, pos.z + seedSpot.z))
        seeds -= 1
      }
    }
    // Get bigger!
    grow
  }
}
