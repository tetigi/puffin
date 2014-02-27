package com.puffin.utils.generation

import javax.imageio._
import java.io._
import java.awt.image.BufferedImage

object BitmapReader {
  def readBitmapFile(filename: String): BufferedImage = {
    var img: BufferedImage = null;
    try {
      img = ImageIO.read(new File(filename))
    } catch {
      case ioe: IOException => println("Got IO Exception whilst reading bitmap " + filename + ioe)
      case _: Exception => println("Got some other exception whilst reading bitmap " + filename)
    }
    img
  }
}
