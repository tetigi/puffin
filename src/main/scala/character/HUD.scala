package com.puffin.character

import org.lwjgl.util.vector.Vector3f

import com.puffin.Common._
import com.puffin.render.RenderableBase
import com.puffin.objects.PlaneCanvas
import com.puffin.context.World

object HUD extends RenderableBase {
  val canvas = new PlaneCanvas(50, 50, new Vector3f(0, 0, 1))


  def render(opts: RenderOptions) {
    val entity = World.entity
    val dir = entity.dir
    val pos = entity.pos

    canvas.setDir(dir.negate(new Vector3f()))
    canvas.setPos(Vector3f.add(pos, dir, new Vector3f()))
    canvas.render(opts)
    // TODO somehow set this so that it always rerenders an everyframe
  }
}
