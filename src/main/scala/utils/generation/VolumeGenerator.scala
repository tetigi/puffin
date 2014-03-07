package com.puffin.utils.generation

import scala.collection.mutable.HashMap

import java.io.IOException

import com.puffin.Common._
import com.puffin.objects.Volume

object VolumeGenerator {
  val RGB_WHITE = -1

  def genVolumeFromBitmap(filename: String): Volume = {
    val bmp = BitmapReader.readBitmapFile(filename)
    // Going to pad the volume by 1 on each side so the whole thing renders
    val (sizeX, sizeY, sizeZ) = (bmp.getWidth(), bmp.getHeight(), 1)
    val vol = new Volume(sizeX + 2, sizeY + 2, sizeZ + 2)


    // Bitmap starts from topleft, whereas volume starts from bottom right, so need to account for that
    for ((x,y) <- xzIn(0, sizeX, sizeY) if (bmp.getRGB(x, y) != RGB_WHITE)) vol.put(sizeX - x, sizeY - y, 1, 1)
      
    vol
  }

  // Going for an 'errosion' approach
  def genVolumeFromBitmaps(faces: Map[String, String]): Volume = {
    if (!List("front", "left", "top")
        .map(faces.contains(_))
        .fold(true)({ (x,y) => x && y }))
      throw new IOException("Could not load all faces of the volume - some faces were missing")

    val front = BitmapReader.readBitmapFile(faces.get("front").get)
    val left  = BitmapReader.readBitmapFile(faces.get("left").get)
    val top  = BitmapReader.readBitmapFile(faces.get("top").get)

    // Get size of whole volume
    val (sizeX, sizeY, sizeZ) = (front.getWidth(), front.getHeight(), left.getWidth())

    // Fill whole volume
    val vol = new Volume(sizeX, sizeY, sizeZ)
    vol.fill()
    
    // Carve out the front
    for ((x, y) <- xzIn(0, sizeX, sizeY))
      if (front.getRGB(x, y) == RGB_WHITE)
        for (z <- 0 until sizeZ)
          vol.put(x, sizeY - y - 1, z, 0)

    // Carve out the left
    for ((z, y) <- xzIn(0, sizeZ, sizeY))
      if (left.getRGB(z, y) == RGB_WHITE)
        for (x <- 0 until sizeX)
          vol.put(x, sizeY - y - 1, z, 0)

    // Carve out the top
    for ((x, z) <- xzIn(0, sizeX, sizeZ))
      if (top.getRGB(x, z) == RGB_WHITE)
        for (y <- 0 until sizeY)
          vol.put(x, y, z, 0)

    vol
  }
}
