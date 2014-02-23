package com.puffin.objects

import scala.math._

import com.puffin.data.Array3D
import com.puffin.Common.Point
import com.puffin.render.Quads
import com.puffin.context.World
import com.puffin.context.BlockType

class Tree(x: Int, y: Int, z: Int) extends SimpleObject {
  def this() = this(0, 0, 0)
  var (dimX, dimY, dimZ) = (3, 4, 3)
  var data = new Array3D[Int](dimX, dimY, dimZ)
  val pos = new Point(x, y, z)

  def getData = data
  def getUsedPoints = for (i <- 0 until height) yield new Point(0, i, 0) + pos
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

  def update() = {
    dimY = height + 1
    // Redraw the tree
    data = new Array3D[Int](dimX, dimY, dimZ)
    for (i <- 0 until height)
      data.put(1, i, 1, 1)
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
