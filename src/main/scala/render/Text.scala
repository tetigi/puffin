package com.puffin.render

import scala.collection.mutable.HashMap

import com.puffin.Common._

object Text {
  val width = 4
  val letters = new HashMap[Char, List[String]]()

  def get(char: Char): List[String] =
    if (letters.contains(char)) letters.get(char).get
    else gap

  def get(str: String): List[String] = {
    var word = List("","","","")
    for ((c, i) <- str.zipWithIndex) {
      val letter = get(c)
      word = zipWith({ (x: String, y: String) => x ++ y }, word, letter) 
      if (i < str.length - 1)
        word = zipWith({ (x: String, y: String) => x ++ y }, word, gap)
    }
    word
  }

  val gap = List( " ",
                  " ",
                  " ",
                  " ")

  letters.put(' ', List("    ",
                        "    ",
                        "    ",
                        "    "))

  letters.put('a', List(" XX ",
                        "X  X",
                        "XXXX",
                        "X  X"))

  letters.put('b', List("    ",
                        "    ",
                        "    ",
                        "    "))
}

// Doesn't do much yet
class Format {
  val size = 1f
}
