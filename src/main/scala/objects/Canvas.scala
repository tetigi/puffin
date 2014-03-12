package com.puffin.objects

import com.puffin.Common.Point2

// Something on which you can draw 2D graphics
trait Canvas {
  type RGB = (Int, Int, Int)
  // Single pixel putters and getters
  def getPixel(x: Int, y: Int): Option[RGB]
  def putPixel(x: Int, y: Int, rgb: RGB): Unit

  // More complex drawing operations
  def drawLine(start: Point2, end: Point2): Unit
  def drawLines(points: List[Point2]): Unit
}
