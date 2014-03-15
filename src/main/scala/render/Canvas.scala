package com.puffin.render

import scala.collection.mutable.Stack
import scala.math._

import com.puffin.Common.Point2

// Something on which you can draw 2D graphics
trait Canvas {
  type RGB = (Int, Int, Int)
  // Single pixel putters and getters
  def getPixel(x: Int, y: Int): Option[RGB]
  def putPixel(x: Int, y: Int, rgb: RGB): Unit

  // More complex drawing operations
  def drawLine(start: Point2, end: Point2) {
    val (dx, dy) = (abs(start.x - end.x), abs(start.y - end.y))
    if (dx == 0) { // Then m == inf
      for (y <- min(start.y, end.y) to max(start.y, end.y))
        putPixel(start.x, y, (0, 0, 0))
    } else {
      val m: Float = (start.y - end.y).toFloat/(start.x - end.x).toFloat
      val c: Float = start.y - ((start.y - end.y).toFloat * start.x.toFloat / (start.x - end.x))
  
      if (dx > dy) {
        for (x <- min(start.x, end.x) to max(start.x, end.x))
          putPixel(x, round(m*x + c), (0, 0, 0))
      } else {
        for (y <- min(start.y, end.y) to max(start.y, end.y))
          putPixel(round((y - c)/m), y, (0, 0, 0))
      }
    }
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
