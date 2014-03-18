package com.puffin.objects

import scala.collection.mutable.ListBuffer

import org.lwjgl.util.vector.Vector3f

import com.puffin.render.{Quads, Canvas}
import com.puffin.data.Array2D
import com.puffin.Common._
import com.puffin.context.World

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

  // Need to work out the left vector and the right vector
  // based on the normal
  // Know one point on the plane (position at bottom left)
  // Need to use this to work out the up and right vectores
  // The bottom right vector should never have a Y component
  // Need to write all this down really on a piece of paper. Maybe can do this at work.
  private class Pixel(val pos: Point2, val norm: Vector3f) {
    // Make this change based on the normal
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
