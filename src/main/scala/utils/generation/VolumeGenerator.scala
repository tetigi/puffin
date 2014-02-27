package com.puffin.utils.generation

import com.puffin.Common._
import com.puffin.objects.Volume

object VolumeGenerator {
  val RGB_WHITE = -1

  def genVolumeFromBitmap(filename: String): Volume = {
    val bmp = BitmapReader.readBitmapFile(filename)
    // Going to pad the volume by 1 on each side so the whole thing renders
    val (sizeX, sizeY, sizeZ) = (bmp.getWidth(), bmp.getHeight(), 1)
    val vol = new Volume(sizeX + 2, sizeY + 2, sizeZ + 2)


    // Bitmap starts from topleft, whereas volume starts from bottom left, so need to account for that
    for ((x,y) <- xzIn(0, sizeX, sizeY) if (bmp.getRGB(x, y) != RGB_WHITE)) vol.put(x, sizeY - y, 1, 1)
      
    vol
  }

  def genVolumeFromBitmaps(frontFile: String, leftFile: String): Volume = {
    val front = BitmapReader.readBitmapFile(frontFile)
    val left  = BitmapReader.readBitmapFile(leftFile)
    
    // Do analysis to get dims and locs and verify
    val (sizeX, sizeY, sizeZ) = (front.getWidth(), front.getHeight(), left.getWidth())
    // Get leftmost pixel

    // These are dependent on the order that xzIn provides coords
    val leftLim = xzIn(0, sizeX, sizeY).dropWhile({ p: (Int, Int) => front.getRGB(p._1, p._2) == RGB_WHITE }).head._1
    val rightLim = xzIn(0, sizeX, sizeY).dropWhile({ p: (Int, Int) => front.getRGB(sizeX - (p._1 + 1), p._2) == RGB_WHITE }).head._1
    val frontLim = xzIn(0, sizeZ, sizeY).dropWhile({ p: (Int, Int) => left.getRGB(p._1, p._2) == RGB_WHITE }).head._1
    val backLim = xzIn(0, sizeZ, sizeY).dropWhile({ p: (Int, Int) => left.getRGB(sizeZ - (p._1 + 1), p._2) == RGB_WHITE }).head._1
    val topLim = xzIn(0, sizeY, sizeX).dropWhile({ p: (Int, Int) => front.getRGB(p._2, p._1) == RGB_WHITE }).head._1
    val bottomLim = xzIn(0, sizeY, sizeX).dropWhile({ p: (Int, Int) => front.getRGB(p._2, sizeY - (p._1 + 1)) == RGB_WHITE }).head._1

    println("Lims are (left, right, front, back, top, bottom) = " + (leftLim, rightLim, frontLim, backLim, topLim, bottomLim))

    // Print into the volume
    val vol = new Volume(sizeX + 2, sizeY + 2, sizeZ + 2)
    
    // front
    for ((x,y) <- xzIn(0, sizeX, sizeY) if (front.getRGB(x, y) != RGB_WHITE)) vol.put(x, sizeY - y, frontLim, 1)
    // back
    for ((x,y) <- xzIn(0, sizeX, sizeY) if (front.getRGB(x, y) != RGB_WHITE)) vol.put(x, sizeY - y, sizeZ - backLim - 1, 1)
    // right
    for ((z, y) <- xzIn(0, sizeZ, sizeY) if (left.getRGB(z, y) != RGB_WHITE)) vol.put(sizeX - rightLim - 1, sizeY - y, z, 1)
    // left
    for ((z, y) <- xzIn(0, sizeZ, sizeY) if (left.getRGB(z, y) != RGB_WHITE)) vol.put(leftLim, sizeY - y, z, 1)

    // TODO Add filling on top and bottom
    // Fill top
    for ((x,z) <- xzIn(leftLim, frontLim, sizeX - rightLim - 1, sizeZ - backLim - 1)) vol.put(x, sizeY - topLim , z, 1)
    for ((x,z) <- xzIn(leftLim, frontLim, sizeX - rightLim - 1, sizeZ - backLim - 1)) vol.put(x, bottomLim + 1, z, 1)
    // TODO Add depth inferred from sides

    vol
  }
}
