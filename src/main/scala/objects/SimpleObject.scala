package com.puffin.objects

import com.puffin.Common.Point3
import com.puffin.render.Quads

trait SimpleObject extends Quads {
  def tick(): Unit
}
