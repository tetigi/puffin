package com.puffin.objects

import scala.collection.mutable.ListBuffer

import org.lwjgl.util.vector.Vector3f

import com.puffin.render.{Quads, Canvas}
import com.puffin.data.Array2D
import com.puffin.Common._
import com.puffin.context.World

object PlaneCanvas {
  def getDirs(n: Vector3f): (Vector3f, Vector3f) = {
    // Work out the horizontal vector h
    val h = new Vector3f(-n.z, 0, n.x)
    h.normalise(h)

    // Work out the vertical vector v
    val v = new Vector3f(n.x*n.y, n.z*n.z - n.x*n.x, -n.z*n.y)
    v.normalise(v)
    (h, v)
  }

}

// TODO Make position do something
class PlaneCanvas(val dimX: Int, val dimY: Int, val position: Point3, val normal: Vector3f) extends Quads with Canvas {
  def this(dX: Int, dY: Int, pos: Point3) = this(dX, dY, pos, new Vector3f(0, 0, -1))

  val data: Array2D[Option[RGB]] = Array2D.initWith(dimX, dimY, { () => None })
  val pixelsPerCube = 40f

  def getDims = (dimX, dimY)

  def createRawQuadData(opts: RenderOptions): RawQuadData = {
    val pixels: ListBuffer[Pixel] = new ListBuffer()
    for ((p, rgb) <- data.iteratorWithKey) {
      if (rgb.nonEmpty) {
        pixels += new Pixel(p, normal)
      }
    }

    val (worldX, worldY, worldZ) = World.size
    val verts = pixels.flatMap(_.toVector3f)
    verts.map({ v: Vector3f => Vector3f.add(v, position.toVector3f, v) })
    verts.map({ v: Vector3f => flatScaleVector3f(v, new Vector3f(1.0f/worldX, 1.0f/worldY, 1.0f/worldZ), v) })
    val flatVerts: Array[Float] = verts.flatMap( v => List(v.x, v.y, v.z)).toArray
    val normals: Array[Float] = repeat(new Vector3f(0, 0, -1), flatVerts.length/3).flatMap(v => List(v.x, v.y, v.z)).toArray
    val occludes: Array[Float] = repeat(0f, flatVerts.length).toArray
    new RawQuadData(flatVerts, normals, occludes)
  }

  private class Pixel(val pos: Point2, val norm: Vector3f) {
    // Make this change based on the normal
    def toVector3f = {
      val (h, v) = PlaneCanvas.getDirs(norm)
      val v1 = new Vector3f(pos.x, pos.y, 0)

      val v2 = new Vector3f()
      Vector3f.add(v1, h, v2)

      val v3 = new Vector3f()
      Vector3f.add(v1, v, v3)

      val v4 = new Vector3f()
      Vector3f.add(v1, v, v4)
      Vector3f.add(v4, h, v4)

      val vecs: List[Vector3f] = List(v1, v2, v4, v3)
      vecs.map({v: Vector3f => scaleVector3f(v, 1f / pixelsPerCube, v)})
    }

  }

  def getPixel(x: Int, y: Int): Option[RGB] =
    data.get(x, y)

  def putPixel(x: Int, y: Int, rgb: RGB) =
    data.put(x, y, Some(rgb))
}
