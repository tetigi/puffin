package com.puffin.objects

import scala.math._

import com.puffin.Common._
import com.puffin.render.Quads
import com.puffin.simplex.SimplexNoise
import com.puffin.data.Array3D

class Volume(val dimSize: Int) extends Quads { 
  val data = new Array3D[Int](dimSize)
  def get(x: Int, y: Int, z: Int) = data.get(x, y, z)
  def put(x: Int, y: Int, z: Int, v: Int) = data.put(x, y, z, v)

  def getDimSize = dimSize
  def getData = data

  def fillRandom(p: Double = 0.5) = {
    //Pick random cells to fill
    val fill = 
      (for ((x, y, z) <- xyzIn(1, dimSize-1))
        yield (x, y, z, random)) filter { _._4 <= clamp(p, 0, 1) }
    fill map { x => put(x._1, x._2, x._3, 1) }
    ()
  }

  def fillSimplexNoise(lim: Double) = {
    val fill = 
      (for {
        (x, y, z) <- xyzIn(1, dimSize-1)
        nx = x.toFloat / dimSize.toFloat
        ny = y.toFloat / dimSize.toFloat
        nz = z.toFloat / dimSize.toFloat
      } yield (x, y, z, SimplexNoise.simplexNoise(1, nx*3, ny*3, nz*3))) filter { _._4 > lim }
    fill map { x => put(x._1, x._2, x._3, 1) }
    ()
  }

  def fillFloatingRock() = {
    println("Filling with floating rock...")
    var progress = 0
    for {
        (x, y, z) <- xyzIn(1, dimSize-1)
        xf = x.toFloat / dimSize.toFloat
        yf = y.toFloat / dimSize.toFloat
        zf = z.toFloat / dimSize.toFloat
    } {
      if (progress % (dimSize*dimSize*dimSize/10) == 0) 
        println(s"${progress*100/(dimSize*dimSize*dimSize)}% complete...")

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
      
      put(x, y, z, if (density > 3.1) 1 else 0)
    }
    println("...Done!")
  }

  def fillIsland() = {
    println("Filling with island...")
    var progress = 0
    for {
        (x, y, z) <- xyzIn(1, dimSize-1)
        xf = x.toFloat / dimSize.toFloat
        yf = y.toFloat / dimSize.toFloat
        zf = z.toFloat / dimSize.toFloat
    } {
      if (progress % (dimSize*dimSize*dimSize/10) == 0) 
        println(s"${progress*100/(dimSize*dimSize*dimSize)}% complete...")

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
      
      put(x, y, z, if (density > 3.1) 1 else 0)
    }
    println("...Done!")
  }

}
