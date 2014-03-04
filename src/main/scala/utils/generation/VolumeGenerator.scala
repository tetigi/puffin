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
    val wallData = new HashMap[(Int, Int), Option[Int]]()
    val wallDataLines = new HashMap[Int, List[(Int, Int)]]()
    for ((x,y) <- xzIn(0, sizeX, sizeY) if (front.getRGB(x, y) != RGB_WHITE)) {
      wallData.put((x, y), None)
      wallDataLines.put(y, (x, y) :: wallDataLines.getOrElse(y, Nil))
    }

    // Fill in Z values
    val topLim = xzIn(0, sizeY, sizeX).dropWhile({ p: (Int, Int) => front.getRGB(p._2, p._1) == RGB_WHITE }).head._1
    val bottomLim = xzIn(0, sizeY, sizeX).dropWhile({ p: (Int, Int) => front.getRGB(p._2, sizeY - (p._1 + 1)) == RGB_WHITE }).head._1
    val backLim = xzIn(0, sizeZ, sizeY).dropWhile({ p: (Int, Int) => left.getRGB(sizeZ - (p._1 + 1), p._2) == RGB_WHITE }).head._1
    val rightLim = xzIn(0, sizeX, sizeY).dropWhile({ p: (Int, Int) => front.getRGB(sizeX - (p._1 + 1), p._2) == RGB_WHITE }).head._1

    for (y <- topLim until (sizeY - bottomLim)) {
      val newZ = (for (z <- 0 until sizeZ) yield z).dropWhile(left.getRGB(_, y) == RGB_WHITE).head
      val fronts: List[(Int, Int)] = wallDataLines.getOrElse(y, Nil)
      for (p <- fronts) wallData.put((p._1, p._2), Some(newZ))
    }
    
    val vol = new Volume(sizeX + 2, sizeY + 2, sizeZ + 2)
    val back = wallData.max._2.get
    for ((x, y) <- wallData.keys) {
      vol.put(x, sizeY - y, wallData.getOrElse((x, y), Some(1)).get, 1)
      vol.put(x, sizeY - y, sizeZ - backLim - 1 + (back - wallData.getOrElse((x, y), Some(1)).get), 1)
    } 
    
    // Fill the side with data without the x coord
    wallData.clear()
    wallDataLines.clear()
    for ((z,y) <- xzIn(0, sizeZ, sizeY) if (left.getRGB(z, y) != RGB_WHITE)) {
      wallData.put((z, y), None)
      wallDataLines.put(y, (z, y) :: wallDataLines.getOrElse(y, Nil))
    }

    // Fill in X values
    for (y <- topLim until (sizeY - bottomLim)) {
      val newX = (for (x <- 0 until sizeX) yield x).dropWhile(front.getRGB(_, y) == RGB_WHITE).head
      val fronts: List[(Int, Int)] = wallDataLines.getOrElse(y, Nil)
      for (p <- fronts) wallData.put((p._1, p._2), Some(newX))
    }
    
    val right = wallData.max._2.get
    for ((z, y) <- wallData.keys) {
      vol.put(wallData.getOrElse((z,y), Some(1)).get, sizeY - y, z, 1)
      vol.put(sizeX - rightLim - 1 + (right - wallData.getOrElse((z,y), Some(1)).get), sizeY - y, z, 1)
    }

    vol
  }
}
