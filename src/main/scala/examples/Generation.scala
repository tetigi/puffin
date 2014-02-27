package com.puffin.examples

import com.puffin.utils.PuffinApp
import com.puffin.utils.generation.VolumeGenerator
import com.puffin.context.World

class Generation extends PuffinApp {
  def setupWorld() {
    //val volume = VolumeGenerator.genVolumeFromBitmap("resources/face.bmp")
    val volume = VolumeGenerator.genVolumeFromBitmap("resources/thing.bmp")
    World.putThing(volume)

    World.entity.moveToCell(0, 10, 20)
    World.entity.lookLat(180.toRadians)
    World.entity.lookLng(20.toRadians)
  }
}

object Generation {
  def main(args : Array[String]) {
    val app = new Generation()
    app.start()
  }
}

