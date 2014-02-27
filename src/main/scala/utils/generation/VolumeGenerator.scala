package com.puffin.utils.generation

import scala.collection.mutable.HashMap

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
    // Fill top
    for ((x,z) <- xzIn(leftLim, frontLim, sizeX - rightLim - 1, sizeZ - backLim - 1)) vol.put(x, sizeY - topLim , z, 1)
    for ((x,z) <- xzIn(leftLim, frontLim, sizeX - rightLim - 1, sizeZ - backLim - 1)) vol.put(x, bottomLim + 1, z, 1)
    // TODO Add depth inferred from sides

    vol
  }

  def genVolumeFromBitmaps2(frontFile: String, leftFile: String): Volume = {
    val front = BitmapReader.readBitmapFile(frontFile)
    val left  = BitmapReader.readBitmapFile(leftFile)
    
    // Do analysis to get dims and locs and verify
    val (sizeX, sizeY, sizeZ) = (front.getWidth(), front.getHeight(), left.getWidth())

    // Fill the front with data without the z coord
    val frontData = new HashMap[(Int, Int), Option[Int]]()
    val frontsData = new HashMap[Int, List[(Int, Int)]]()
    for ((x,y) <- xzIn(0, sizeX, sizeY) if (front.getRGB(x, y) != RGB_WHITE)) {
      frontData.put((x, y), None)
      frontsData.put(y, (x, y) :: frontsData.getOrElse(y, Nil))
    }

    // Fill in Z values
    val topLim = xzIn(0, sizeY, sizeX).dropWhile({ p: (Int, Int) => front.getRGB(p._2, p._1) == RGB_WHITE }).head._1
    val bottomLim = xzIn(0, sizeY, sizeX).dropWhile({ p: (Int, Int) => front.getRGB(p._2, sizeY - (p._1 + 1)) == RGB_WHITE }).head._1

    for (y <- topLim until (sizeY - bottomLim)) {
      val newZ = (for (z <- 0 until sizeZ) yield z).dropWhile(left.getRGB(_, y) == RGB_WHITE).head
      val fronts: List[(Int, Int)] = frontsData.getOrElse(y, Nil)
      for (p <- fronts) frontData.put((p._1, p._2), Some(newZ))
    }
    
    val vol = new Volume(sizeX + 2, sizeY + 2, sizeZ + 2)
    for ((x, y) <- frontData.keys) 
      vol.put(x, y, frontData.getOrElse((x, y), Some(1)).get, 1)
    
    vol
  }


}
