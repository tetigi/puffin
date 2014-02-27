package com.puffin.examples

import com.puffin.utils.PuffinApp
import com.puffin.objects.Volume
import com.puffin.objects.Tree
import com.puffin.Common._
import com.puffin.context.World

class Vegetation extends PuffinApp {
  val volume = new Volume(DEFAULT_SIZE, 20, DEFAULT_SIZE)

  def setupWorld() {
    
    // Turn off swiveling
    rotateOn = false

    //volume.fillSimplexNoise(1.1)
    volume.fillSmallHills()
    World.putThing(volume)

    val tree = new Tree(0, 0, 0)
    tree.adultTree()
    World.putThing(tree)

    World.entity.moveToCell(0, 20, -40)
    World.entity.lookLng(20.toRadians)
  }
}

object Vegetation {
  def main(args : Array[String]) = {
    val app = new Vegetation()
    app.init()
    app.setupWorld()
    app.start()
  }
}

