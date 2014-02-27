package com.puffin.examples

import com.puffin.utils.PuffinApp
import com.puffin.objects.Volume
import com.puffin.context.World

class Terrain extends PuffinApp {
  // The array containing volume data
  val volume = new Volume(DEFAULT_SIZE)

  def setupWorld() {
    
    //volume.fillRandom(0.5)
    volume.fillSimplexNoise(1.1)
    //volume.fillFloatingRock()
    //volume.fillIsland()
    World.putThing(volume)
  }
}

object Terrain {
  /*
  def main(args : Array[String]) = {
    val app = new Terrain()
    app.start()
  }
  */
}

