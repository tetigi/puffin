package com.puffin.examples

import scala.collection.immutable.HashMap

import com.puffin.Common._
import com.puffin.utils.PuffinApp
import com.puffin.utils.generation.VolumeGenerator
import com.puffin.avro.objects.SimpleObjectInflater
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
    /*val faces = 
      new HashMap() ++ 
        List("front" -> "resources/tree_front.bmp", 
             "left" -> "resources/tree_left.bmp", 
             "top" -> "resources/tree_top.bmp")
    val volume = VolumeGenerator.genVolumeFromBitmaps(faces)
    */
    val treeVol = SimpleObjectInflater.inflateFile("resources/tree.avro")
    val sphereVol = SimpleObjectInflater.inflateFile("resources/sphere.avro")
    sphereVol.getPosition.set(new Point(-10, 0, -10))
    treeVol.getPosition.set(new Point(10, 0, -5))
    World.putThing(treeVol)
    World.putThing(sphereVol)

    World.entity.moveToCell(0, 10, 20)
    World.entity.lookLat(180.toRadians)
    World.entity.lookLng(20.toRadians)
  }
}

object Generation {
  //def main(args : Array[String]) = {
  //  val app = new Generation()
  //  app.start()
  //}
}

