package com.puffin.objects

import scala.collection.mutable.ListBuffer

import org.lwjgl.util.vector.Vector3f

import com.puffin.render.{Quads, Canvas}
import com.puffin.data.Array2D
import com.puffin.Common._
import com.puffin.context.World

// TODO Make position do something
class PlaneCanvas(val dimX: Int, val dimY: Int, val position: Point3) extends Quads with Canvas {
  val data: Array2D[Option[RGB]] = Array2D.initWith(dimX, dimY, { () => None })
  val pixelsPerCube = 4f

  def createRawQuadData(opts: RenderOptions): RawQuadData = {
    val pixels: ListBuffer[Pixel] = new ListBuffer()
    for ((p, rgb) <- data.iteratorWithKey) {
      if (rgb.nonEmpty) {
        pixels += new Pixel(p)
      }
    }

    val (worldX, worldY, worldZ) = World.size
    val verts = pixels.flatMap(_.toVector3f)
    verts.map({ v: Vector3f => flatScaleVector3f(v, new Vector3f(1.0f/worldX, 1.0f/worldY, 1.0f/worldZ), v) })
    val flatVerts: Array[Float] = verts.flatMap( v => List(v.x, v.y, v.z)).toArray
    val normals: Array[Float] = repeat(new Vector3f(0, 0, -1), flatVerts.length/3).flatMap(v => List(v.x, v.y, v.z)).toArray
    val occludes: Array[Float] = repeat(0f, flatVerts.length).toArray
    new RawQuadData(flatVerts, normals, occludes)
  }

  private class Pixel(val pos: Point2) {
    def toVerts = 
      List( pos.x, pos.y, 0,
            pos.x + 1, pos.y, 0,
            pos.x + 1, pos.y + 1, 0,
            pos.x, pos.y + 1, 0).map(_ * pixelsPerCube)

    def toVector3f = {
      val vecs: List[Vector3f] = 
          List( new Vector3f(pos.x, pos.y, 0),
            new Vector3f(pos.x + 1, pos.y, 0),
            new Vector3f(pos.x + 1, pos.y + 1, 0),
            new Vector3f(pos.x, pos.y + 1, 0))
      vecs.map({v: Vector3f => scaleVector3f(v, 1f / pixelsPerCube, v)})
    }
      
  }

  def getPixel(x: Int, y: Int): Option[RGB] =
    data.get(x, y)

  def putPixel(x: Int, y: Int, rgb: RGB) =
    data.put(x, y, Some(rgb))
}
