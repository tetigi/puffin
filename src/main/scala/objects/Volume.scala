package com.puffin.objects

import scala.math._
import scala.collection.mutable.ListBuffer

import com.puffin.Common._
import com.puffin.render.Quads
import com.puffin.simplex.SimplexNoise
import com.puffin.data.Array3D

class Volume(val dimX: Int, val dimY: Int, val dimZ: Int) extends SimpleObject { 
  def this(dim: Int) = this(dim, dim, dim)
  val data = new Array3D[Int](dimX, dimY, dimZ)
  def get(x: Int, y: Int, z: Int) = data.get(x, y, z)
  def put(x: Int, y: Int, z: Int, v: Int) = data.put(x, y, z, v)

  def getDims = (dimX, dimY, dimZ)
  var usedPoints: ListBuffer[Point] = new ListBuffer()
  def getUsedPoints = usedPoints
  def getData = data
  def getPosition = new Point(-dimX/2, -dimY/2, -dimZ/2)

  def tick {}

  def fillRandom(p: Double = 0.5) {
    //Pick random cells to fill
    val fill = 
      (for ((x, y, z) <- xyzIn(1, dimX-1, dimY-1, dimZ-1))
        yield (x, y, z, random)) filter { _._4 <= clamp(p, 0, 1) }
    fill map { x => put(x._1, x._2, x._3, 1); usedPoints += new Point(x._1, x._2, x._3) + getPosition  }
    ()
  }

  def fillSimplexNoise(lim: Double) {
    val fill = 
      (for {
        (x, y, z) <- xyzIn(1, dimX-1, dimY-1, dimZ-1)
        nx = x.toFloat / dimX.toFloat
        ny = y.toFloat / dimY.toFloat
        nz = z.toFloat / dimZ.toFloat
      } yield (x, y, z, SimplexNoise.simplexNoise(1, nx*3, ny*3, nz*3))) filter { _._4 > lim }
    fill map { x => put(x._1, x._2, x._3, 1); usedPoints += new Point(x._1, x._2, x._3) + getPosition }
    ()
  }

  def fillSmallHills() {
    val fill = 
      (for {
        (x, z) <- xzIn(1, dimX-1, dimZ-1)
        nx = x.toFloat / dimX.toFloat
        nz = z.toFloat / dimZ.toFloat
      } yield (x, dimY/2 + round(SimplexNoise.simplexNoise(3, nx, nz)).toInt, z))
    fill map { x => put(x._1, x._2, x._3, 1); usedPoints += new Point(x._1, x._2, x._3) + getPosition }
  }

  def fillFloatingRock() {
    println("Filling with floating rock...")
    var progress = 0
    for {
        (x, y, z) <- xyzIn(1, dimX-1, dimY-1, dimZ-1)
        xf = x.toFloat / dimX.toFloat
        yf = y.toFloat / dimY.toFloat
        zf = z.toFloat / dimZ.toFloat
    } {
      if (progress % (dimX*dimY*dimZ/10) == 0) 
        println(s"${progress*100/(dimX*dimY*dimZ)}% complete...")

      progress += 1
      var plateauFalloff = 0.0
      if (yf <= 0.8) plateauFalloff = 1.0
      else if (0.8 < yf && yf < 0.9) plateauFalloff = 1.0 - (yf - 0.8)*10.0

      val centerFalloff = 0.1/(
        pow((xf-0.5)*1.5, 2) +
        pow((yf-1.0)*0.8, 2) +
        pow((zf-0.5)*1.5, 2))

      var density = SimplexNoise.simplexNoise(5, xf, yf*0.5, zf) *
        centerFalloff * plateauFalloff
      density *= pow(
        SimplexNoise.noise((xf+1)*3.0, (yf+1)*3.0, (zf+1)*3.0) + 0.4, 1.8)
      
      if (density > 3.1) 
        put(x, y, z, 1); usedPoints += new Point(x, y, z) + getPosition
    }
    println("...Done!")
  }

  def fillIsland() = {
    println("Filling with island...")
    var progress = 0
    for {
        (x, y, z) <- xyzIn(1, dimX-1, dimY-1, dimZ-1)
        xf = x.toFloat / dimX.toFloat
        yf = y.toFloat / dimY.toFloat
        zf = z.toFloat / dimZ.toFloat
    } {
      if (progress % (dimX*dimY*dimZ/10) == 0) 
        println(s"${progress*100/(dimX*dimY*dimZ)}% complete...")

      progress += 1
      var plateauFalloff = 0.0
      if (0.4 <= yf && yf <= 0.5) plateauFalloff = 1.0
      else if (0.5 < yf && yf < 0.6) plateauFalloff = 1.0 - (yf - 0.6)*10.0

      val centerFalloff = 0.1/(
        pow((xf-0.5)*1.5, 2) +
        pow((yf-0.5)*1.5, 2) +
        pow((zf-0.5)*1.5, 2))

      var density = SimplexNoise.simplexNoise(5, xf, yf*0.5, zf) *
        centerFalloff * plateauFalloff
      density *= pow(
        SimplexNoise.noise((xf+1)*3.0, (yf+1)*3.0, (zf+1)*3.0) + 0.4, 1.8)
      
      if (density > 3.1) 
        put(x, y, z, 1); usedPoints += new Point(x, y, z) + getPosition
    }
    println("...Done!")
  }

}
