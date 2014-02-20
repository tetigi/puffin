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
  def getUsedPoints = for (i <- 0 until height) yield new Point(1, i, 1) + pos
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
    var seeds = 1
    val seedProb = 0.2
    for (i <- -2 to 2) {
      // -2X    
      // Check there is ground and isn't occupied
      var isGround = World.getRelative(this, -2, -1, i).blockType == BlockType.GROUND
      var isNotOccupied = !World.getOccupiedRelative(this, -2, 0, i)
      if (isGround && isNotOccupied && seeds > 0 && random < seedProb) {
        World.putObject(-2, 0, i, new Tree(pos.x - 2, 0, pos.z + i))
        seeds -= 1
      }
      // -2Z    
      // Check there is ground and isn't occupied
      isGround = World.getRelative(this, i, -1, -2).blockType == BlockType.GROUND
      isNotOccupied = World.getOccupiedRelative(this, i, 0, -2)
      if (isGround && isNotOccupied && seeds > 0 && random < seedProb) {
        World.putObject(i, 0, -2, new Tree(pos.x + i, 0, pos.z - 2))
        seeds -= 1
      }
      // +2X    
      // Check there is ground and isn't occupied
      isGround = World.getRelative(this, 2, -1, i).blockType == BlockType.GROUND
      isNotOccupied = World.getOccupiedRelative(this, 2, 0, i)
      if (isGround && isNotOccupied && seeds > 0 && random < seedProb) {
        World.putObject(2, 0, i, new Tree(pos.x + 2, 0, pos.z + i))
        seeds -= 1
      }
      // +2Z    
      // Check there is ground and isn't occupied
      isGround = World.getRelative(this, i, -1, 2).blockType == BlockType.GROUND
      isNotOccupied = World.getOccupiedRelative(this, i, 0, 2)
      if (isGround && isNotOccupied && seeds > 0 && random < seedProb) {
        World.putObject(i, 0, 2, new Tree(pos.x + i, 0, pos.z + 2))
        seeds -= 1
      }
    }
    // Get bigger!
    grow
  }
}
