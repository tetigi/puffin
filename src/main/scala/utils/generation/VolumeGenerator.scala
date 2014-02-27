package com.puffin.utils.generation

import com.puffin.Common._
import com.puffin.objects.Volume

object VolumeGenerator {
  def genVolumeFromBitmap(filename: String): Volume = {
    val bmp = BitmapReader.readBitmapFile(filename)
    // Going to pad the volume by 1 on each side so the whole thing renders
    val (sizeX, sizeY, sizeZ) = (bmp.getWidth(), bmp.getHeight(), 1)
    val vol = new Volume(sizeX + 2, sizeY + 2, sizeZ + 2)

    val RGB_WHITE = -1

    // Bitmap starts from topleft, whereas volume starts from bottom right, so need to account for that
    for ((x,y) <- xzIn(0, sizeX, sizeY) if (bmp.getRGB(x, y) != RGB_WHITE)) vol.put(sizeX - x, sizeY - y, 1, 1)
      
    vol
  }
}
