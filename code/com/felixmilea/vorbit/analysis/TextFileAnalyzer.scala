package com.felixmilea.vorbit.analysis

import scala.io.Source
import com.felixmilea.vorbit.utils.AppUtils
import akka.actor.Props
import akka.routing.SmallestMailboxRouter

object TextFileAnalyzer extends App {
  val textFile = "appdata/sources/bible.txt"
  val dataSet = "biblebot"
  val strategy = WordOnlyStrategy
  val parser = new TextUnitParser(strategy)

  val text = Source.fromFile(textFile).mkString.split("[\\s]{3,}")

  for (i <- (0 until text.length).par) {
    //    AppUtils.actor("TextUnitParser") ! TextUnitProcessor.Text(text(i), dataSet, strategy)
  }

}