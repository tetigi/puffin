package com.puffin.objects

import scala.math._
import scala.collection.mutable.ListBuffer

import com.puffin.data.Array3D
import com.puffin.Common.Point
import com.puffin.render.Quads
import com.puffin.context.World
import com.puffin.context.BlockType

class Tree(x: Int, y: Int, z: Int) extends SimpleObject {
  def this() = this(0, 0, 0)
  var (dimX, dimY, dimZ) = (1, 3, 1)
  var data = new Array3D[Int](dimX, dimY, dimZ)
  val pos = new Point(x, y, z)

  def getData = data
  val usedPoints: ListBuffer[Point] = new ListBuffer()
  def getUsedPoints = usedPoints
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
    // Fill trunk
    val offset = 16
    for (i <- 0 until 16) {
      // fill out a square for the trunk
      for (j <- offset until (offset + 3)) {
        for (k <- offset until (offset + 3)) {
          data.put(j, i, k, 1)
        }
      }
    }
    // Cube for leaves TODO make not shit
    for (i <- 16 until 28) {
      for (j <- offset - 12 to offset + 12) {
        for (k <- offset - 12 to offset + 12) {
          if (random < 0.7) data.put(j, i, k, 1)
        }
      }
    }

    // Add some vines
    for (i <- 0 until 3) {
      data.put(offset-3, i, offset+6, 1)
      data.put(offset-3, i, offset-3, 1)
      data.put(offset+3, i, offset+5, 1)
    }
  }

  def update() {
    usedPoints.clear()
    dimY = height
    // Redraw the tree
    data = new Array3D[Int](dimX, dimY, dimZ)
    for (i <- 0 until height)
      data.put(0, i, 0, 1)
    for (i <- 0 until height) usedPoints += new Point(0, i, 0) + pos
    requiresRefresh = true
  }

  def spread() = {
    // place seeds in random circle
    var seeds = 4
    val seedProb = 0.2
    for (i <- -2 to -2) {
      // -2X    
      // Check there is ground and isn't occupied
      var seedSpot = new Point(-2, 0, i)
      var isGround = World.getRelative(this, seedSpot.x, seedSpot.y - 1, seedSpot.z).blockType == BlockType.GROUND
      var isNotOccupied = !World.getOccupiedRelative(this, seedSpot.x, seedSpot.y, seedSpot.z)
      if (isGround && isNotOccupied && seeds > 0 && random < seedProb) {
        World.putThing(new Tree(pos.x + seedSpot.x, pos.y + seedSpot.y, pos.z + seedSpot.z))
        seeds -= 1
      }
      // -2Z    
      // Check there is ground and isn't occupied
      seedSpot = new Point(2, 0, i)
      isGround = World.getRelative(this, seedSpot.x, seedSpot.y - 1, seedSpot.z).blockType == BlockType.GROUND
      isNotOccupied = !World.getOccupiedRelative(this, seedSpot.x, seedSpot.y, seedSpot.z)
      if (isGround && isNotOccupied && seeds > 0 && random < seedProb) {
        World.putThing(new Tree(pos.x + seedSpot.x, pos.y + seedSpot.y, pos.z + seedSpot.z))
        seeds -= 1
      }
      // +2X    
      // Check there is ground and isn't occupied
      seedSpot = new Point(-2, 0, -i)
      isGround = World.getRelative(this, seedSpot.x, seedSpot.y - 1, seedSpot.z).blockType == BlockType.GROUND
      isNotOccupied = !World.getOccupiedRelative(this, seedSpot.x, seedSpot.y, seedSpot.z)
      if (isGround && isNotOccupied && seeds > 0 && random < seedProb) {
        World.putThing(new Tree(pos.x + seedSpot.x, pos.y + seedSpot.y, pos.z + seedSpot.z))
        seeds -= 1
      }
      // +2Z    
      // Check there is ground and isn't occupied
      seedSpot = new Point(2, 0, -i)
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
