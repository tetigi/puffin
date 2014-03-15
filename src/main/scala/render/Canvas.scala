package com.puffin.render

import scala.collection.mutable.Stack

import com.puffin.Common.Point2

// Something on which you can draw 2D graphics
trait Canvas {
  type RGB = (Int, Int, Int)
  // Single pixel putters and getters
  def getPixel(x: Int, y: Int): Option[RGB]
  def putPixel(x: Int, y: Int, rgb: RGB): Unit

  // More complex drawing operations
  def drawLine(start: Point2, end: Point2) {
    println("lol")
  }
    
  def drawLines(points: List[Point2]) {
    if (!points.isEmpty) {
      val elems = new Stack() ++ points
      while (elems.size > 1) {
        drawLine(elems.pop(), elems.top)
      }
    }
  }

  def write(pos: Point2, text: String, fmt: Format) {
    println("lol")
  }
}
