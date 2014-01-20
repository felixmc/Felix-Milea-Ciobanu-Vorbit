package com.felixmilea.vorbit.analysis

object WordParser {

  def parse(text: String, print: Boolean = false): List[String] = {
    val words = text.toLowerCase.split("[ \\.,*_()^:\n\r?!\"/\\[\\]]+").toList.filterNot(s => s.isEmpty).removeDuplicates

    if (print) {
      //      println(words.mkString("[", "], [", "]"))
    }

    return words
  }

  def parseAsText(text: String): String = {
    return parse(text).mkString("[", "], [", "]")
  }

  def main(args: Array[String]) {

    val text = "A mountain is a large landform that stretches above the surrounding land in a limited area, usually in the form of a peak. A mountain is generally steeper than a hill. Mountains are formed through tectonic forces or volcanism. These forces can locally raise the surface of the earth by over 10,000 feet (3,000 m). Mountains erode slowly through the action of rivers, weather conditions, and glaciers. A few mountains are isolated summits, but most occur in huge mountain ranges."

    parse(text, true)

  }

}