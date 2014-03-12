package com.puffin.objects

import com.puffin.data.Array2D
import com.puffin.Common._

class PlaneCanvas(val dimX: Int, val dimY: Int, val position: Point3) extends Canvas {
  val data: Array2D[Option[RGB]] = Array2D.initWith(dimX, dimY, { () => None })

  def getPixel(x: Int, y: Int): Option[RGB] =
    data.get(x, y)

  def putPixel(x: Int, y: Int, rgb: RGB) =
    data.put(x, y, Some(rgb))

  def drawLine(start: Point2, end: Point2) {
  }

  def drawLines(points: List[Point2]) {
  }
}
