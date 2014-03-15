package com.puffin.examples


import com.puffin.utils.PuffinApp
import com.puffin.objects.PlaneCanvas
import com.puffin.context.World
import com.puffin.Common._

class Canvas extends PuffinApp {
  def setupWorld() {
    rotateOn = false
    val canvas = new PlaneCanvas(20, 20, Point3(0, 0, 0))
    for (i <- 2 until 18) {
      canvas.putPixel(i, i, (0, 0, 0))
      canvas.putPixel(i, 19-i, (0, 0, 0))
    }
    registerRenderable(canvas)
    World.entity.moveToCell(2, 2, -5)
  }
}

object Canvas {
  def main(args : Array[String]) = {
    val app = new Canvas()
    app.start()
  }
}

