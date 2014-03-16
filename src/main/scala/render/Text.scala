package com.puffin.render

import scala.collection.mutable.HashMap

import com.puffin.Common._

object Text {
  val width = 5
  val letters = new HashMap[Char, List[String]]()

  def get(char: Char): List[String] =
    if (letters.contains(char.toLower)) letters.get(char.toLower).get
    else gap

  def get(str: String): List[String] = {
    var word = List("","","","","")
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
                  " ",
                  " ")

  letters.put(' ', List("     ",
                        "     ",
                        "     ",
                        "     ",
                        "     "))

  letters.put('a', List(" XXX ",
                        "X   X",
                        "X   X",
                        "XXXXX",
                        "X   X"))

  letters.put('b', List("XXXX ",
                        "X   X",
                        "X XX ",
                        "X   X",
                        "XXXXX"))

  letters.put('c', List(" XXX ",
                        "X   X",
                        "X    ",
                        "X   X",
                        " XXX "))

  letters.put('d', List("XXXX ",
                        "X   X",
                        "X   X",
                        "X   X",
                        "XXXX "))

  letters.put('e', List("XXXXX",
                        "X    ",
                        "XXX  ",
                        "X    ",
                        "XXXXX"))

  letters.put('f', List("XXXXX",
                        "X    ",
                        "XXXX ",
                        "X    ",
                        "X    "))

  letters.put('g', List("XXXX ",
                        "X    ",
                        "X XXX",
                        "X  X ",
                        "XXXX "))

  letters.put('h', List("X   X",
                        "X   X",
                        "XXXXX",
                        "X   X",
                        "X   X"))

  letters.put('i', List("XXXXX",
                        "  X  ",
                        "  X  ",
                        "  X  ",
                        "XXXXX"))

  letters.put('j', List(" XXXX",
                        "   X ",
                        "   X ",
                        "X  X ",
                        " XX  "))

  letters.put('k', List("X   X",
                        "X XX ",
                        "XX   ",
                        "X XX ",
                        "X   X"))

  letters.put('l', List("X    ",
                        "X    ",
                        "X    ",
                        "X    ",
                        "XXXXX"))

  letters.put('m', List("X   X",
                        "XX XX",
                        "X X X",
                        "X   X",
                        "X   X"))

  letters.put('n', List("X   X",
                        "XX  X",
                        "X X X",
                        "X  XX",
                        "X   X"))

  letters.put('o', List(" XXX ",
                        "X   X",
                        "X   X",
                        "X   X",
                        " XXX "))

  letters.put('p', List("XXXX ",
                        "X   X",
                        "XXXX ",
                        "X    ",
                        "X    "))

  letters.put('q', List(" XX  ",
                        "X  X ",
                        "X  X ",
                        "X  XX",
                        " XX X"))

  letters.put('r', List("XXXX ",
                        "X   X",
                        "XXXX ",
                        "X  X ",
                        "X   X"))

  letters.put('s', List(" XXXX",
                        "X    ",
                        " XXX ",
                        "    X",
                        "XXXX "))

  letters.put('t', List("XXXXX",
                        "  X  ",
                        "  X  ",
                        "  X  ",
                        "  X  "))

  letters.put('u', List("X   X",
                        "X   X",
                        "X   X",
                        "X   X",
                        " XXX "))

  letters.put('v', List("X   X",
                        "X   X",
                        "X   X",
                        " X X ",
                        "  X  "))

  letters.put('w', List("X   X",
                        "X   X",
                        "X X X",
                        "X X X",
                        " X X "))

  letters.put('x', List("X   X",
                        " X X ",
                        "  X  ",
                        " X X ",
                        "X   X"))

  letters.put('y', List("X   X",
                        " X X ",
                        "  X  ",
                        " X   ",
                        "X    "))

  letters.put('z', List("XXXXX",
                        "   XX",
                        "  X  ",
                        "XX   ",
                        "XXXXX"))

  letters.put('0', List(" XXX ",
                        "X  XX",
                        "X X X",
                        "XX  X",
                        " XXX "))

  letters.put('1', List(" XXX ",
                        "X XX ",
                        "  XX ",
                        "  XX ",
                        "XXXXX"))

  letters.put('2', List(" XXXX",
                        "X   X",
                        "  XX ",
                        "X    ",
                        "XXXXX"))

  letters.put('3', List("XXXXX",
                        "    X",
                        " XXXX",
                        "    X",
                        "XXXXX"))

  letters.put('4', List("X   X",
                        "X   X",
                        "XXXXX",
                        "    X",
                        "    X"))

  letters.put('5', List("XXXXX",
                        "X    ",
                        "XXXX ",
                        "    X",
                        "XXXX "))

  letters.put('6', List("XXXXX",
                        "X    ",
                        "XXXXX",
                        "X   X",
                        "XXXXX"))

  letters.put('7', List("XXXXX",
                        "    X",
                        "   X ",
                        "  X  ",
                        " X   "))

  letters.put('8', List("XXXXX",
                        "X   X",
                        " XXX ",
                        "X   X",
                        "XXXXX"))

  letters.put('9', List("XXXXX",
                        "X   X",
                        "XXXXX",
                        "    X",
                        "XXXXX"))

}

// Doesn't do much yet
class Format {
  val size = 1f
}
