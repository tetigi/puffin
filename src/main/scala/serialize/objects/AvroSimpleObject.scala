package com.puffin.avro.objects

import com.gensler.scalavro.types.AvroType
import com.gensler.scalavro.io.AvroTypeIO

import scala.collection.mutable.{Set, HashSet}
import scala.util.Success

import com.puffin.Common.Point3
import com.puffin.objects._
import com.puffin.data.Array3D

object ObjectType extends Enumeration {
  type ObjectType = Value
  val TREE, VOLUME = Value
}

import ObjectType._

case class SimpleObject(
  objectType: ObjectType, 
  position: Point3,
  points: Seq[Point3],
  metadata: Map[String, String]
)

object SimpleObjectInflater {
  val simpleObjectType = AvroType[SimpleObject]
  val io: AvroTypeIO[SimpleObject] = simpleObjectType.io

  def inflateFile(filename: String): com.puffin.objects.SimpleObject = {
    val input = new java.io.FileInputStream(filename)
    val readObject = io read input
    val out = readObject match {
      case Success(obj) => readSimpleObject(obj)
      case _ => throw new java.io.IOException("Could not read object from " + filename)
    }
    input.close()
    out
  }

  def deflateObj[T <: com.puffin.objects.SimpleObject](obj: T, deflater: InflateableSimpleObject[T], outfile: String) {
    val output = new java.io.FileOutputStream(outfile)
    io.write(deflater.deflate(obj), output)
    output.flush()
    output.close()
  }

  def readSimpleObject(obj: SimpleObject): com.puffin.objects.SimpleObject = {
    obj.objectType match {
      case VOLUME => Volume.inflate(obj)
      case TREE => Tree.inflate(obj)
    }
  }
}

trait InflateableSimpleObject[T <: com.puffin.objects.SimpleObject] {
  def deflate(obj: T): SimpleObject
  def inflate(obj: SimpleObject): T

  def rebuildData(points: Seq[Point3]): Array3D[Int] = {
    val (minX, minY, minZ) = (points.map(_.x).min, points.map(_.y).min, points.map(_.z).min)
    val (maxX, maxY, maxZ) = (points.map(_.x).max, points.map(_.y).max, points.map(_.z).max)
    val (dimX, dimY, dimZ) = (maxX - minX + 1, maxY - minY + 1, maxZ - minZ + 1)
    val data = new Array3D[Int](dimX, dimY, dimZ)
    for (p <- points)
      data.put(p.x - minX, p.y - minY, p.z - minZ, 1)
    data
  }
}
