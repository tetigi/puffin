package com.puffin.examples

import com.puffin.utils.PuffinApp
import com.puffin.objects.Volume
import com.puffin.objects.Tree
import com.puffin.objects.Plane
import com.puffin.context.World

class Vegetation extends PuffinApp {
  val volume = new Volume(DEFAULT_SIZE, 20, DEFAULT_SIZE)
  val plane = new Plane(DEFAULT_SIZE, DEFAULT_SIZE)

  def setupWorld() {
    
    // Turn off swiveling
    rotateOn = false

    //volume.fillSimplexNoise(1.1)
    volume.fillSmallHills()
    World.putThing(volume)
    //World.putThing(plane)

    val tree = new Tree(0, 0, 0)
    tree.adultTree()
    World.putThing(tree)

    World.entity.moveToCell(0, 20, -40)
    World.entity.lookLng(20.toRadians)
  }
}

object Vegetation {
  //def main(args: Array[String]) {
  // val app = new Vegetation()
  //  app.start()
  //}
}

