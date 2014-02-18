package com.puffin.render

import com.puffin.Common._

trait RenderableBase {
  val default = new RenderOptions()
  
  def render(opts: RenderOptions): Unit
  def render(): Unit = render(default) 
}
