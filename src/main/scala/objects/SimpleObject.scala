package com.puffin.objects

import com.puffin.Common.Point3
import com.puffin.render.Cubes

trait SimpleObject extends Cubes {
  def tick(): Unit
}
