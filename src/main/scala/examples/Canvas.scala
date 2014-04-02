package com.puffin.examples

import org.lwjgl.util.vector.Vector3f

import com.puffin.render.Format
import com.puffin.utils.PuffinApp
import com.puffin.objects.PlaneCanvas
import com.puffin.context.World
import com.puffin.Common._

class Canvas extends PuffinApp {
  def setupWorld() {
    rotateOn = false
    val canvas = new PlaneCanvas(100, 20, new Vector3f(0, 1, 0), new Vector3f(0, -1, -0.01f))
    /*
    for (i <- 2 until 18) {
      canvas.putPixel(i, i, (0, 0, 0))
      canvas.putPixel(i, 19-i, (0, 0, 0))
    }
    */
   /*
   canvas.drawLine(Point2(2,2), Point2(18,18))
   canvas.drawLine(Point2(18,2), Point2(2,18))

   canvas.drawLine(Point2(2,2), Point2(18,9))
   canvas.drawLine(Point2(2,18), Point2(18,10))

   canvas.drawLine(Point2(2,2), Point2(18,2))
   canvas.drawLine(Point2(2,2), Point2(2,18))
   canvas.drawLine(Point2(18,18), Point2(18,2))
   canvas.drawLine(Point2(18,18), Point2(2,18))
   */

    canvas.write(Point2(2,10), "luke 123 sylwi", new Format())
    registerRenderable(canvas)
    World.entity.moveToCell(2, 0, -3)
  }
}

object Canvas {
  def main(args : Array[String]) = {
    val app = new Canvas()
    app.start()
  }
}

