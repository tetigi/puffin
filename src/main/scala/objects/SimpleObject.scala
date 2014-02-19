package com.puffin.objects

import com.puffin.Common.Point
import com.puffin.render.Quads

trait SimpleObject extends Quads {
  def getPosition: Point
}
