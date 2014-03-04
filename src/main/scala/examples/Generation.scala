package com.puffin.examples

import scala.collection.immutable.HashMap

import com.puffin.utils.PuffinApp
import com.puffin.utils.generation.VolumeGenerator
import com.puffin.context.World

class Generation extends PuffinApp {
  def setupWorld() {
    //val volume = VolumeGenerator.genVolumeFromBitmap("resources/face.bmp")
    //val volume = VolumeGenerator.genVolumeFromBitmap("resources/tree_front.bmp")
    //val volume = VolumeGenerator.genVolumeFromBitmap("resources/face3D_front.bmp")
    //val volume = VolumeGenerator.genVolumeFromBitmap("resources/thing.bmp")
    //val volume = VolumeGenerator.genVolumeFromBitmaps("resources/tv_front.bmp", "resources/tv_left.bmp")
    //val volume = VolumeGenerator.genVolumeFromBitmaps("resources/face3D_front.bmp", "resources/face3D_left.bmp")
    //val volume = VolumeGenerator.genVolumeFromBitmaps("resources/sphere.bmp", "resources/sphere.bmp")
    val faces = 
      new HashMap() ++ 
        List("front" -> "resources/sphere.bmp", 
             "back" -> "resources/sphere.bmp",
             "left" -> "resources/sphere.bmp", 
             "right" -> "resources/sphere.bmp", 
             "top" -> "resources/sphere.bmp", 
             "bottom" -> "resources/sphere.bmp")
    val volume = VolumeGenerator.genVolumeFromBitmaps2(faces)
    World.putThing(volume)

    World.entity.moveToCell(0, 10, -20)
    World.entity.lookLng(20.toRadians)
  }
}

object Generation {
  def main(args : Array[String]) {
    val app = new Generation()
    app.start()
  }
}
