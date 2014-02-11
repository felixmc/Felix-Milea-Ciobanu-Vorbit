package com.felixmilea.vorbit.analysis

import scala.io.Source
import com.felixmilea.vorbit.utils.App
import akka.actor.Props
import com.felixmilea.vorbit.data.TextUnitProcessor
import com.felixmilea.vorbit.data.BigramParser
import com.felixmilea.vorbit.data.TrigramParser
import com.felixmilea.vorbit.data.QuadgramParser
import akka.routing.SmallestMailboxRouter

object TextFileAnalyzer extends App {
  val textFile = "appdata/sources/bible.txt"
  val dataSet = "biblebot"
  val strategy = WordOnlyStrategy
  val parser = new TextUnitParser(strategy)

  App.actorSystem.actorOf(Props[TextUnitProcessor].withRouter(SmallestMailboxRouter(20)), "TextUnitParser")
  App.actorSystem.actorOf(Props[BigramParser].withRouter(SmallestMailboxRouter(15)), "BigramParser")
  App.actorSystem.actorOf(Props[TrigramParser].withRouter(SmallestMailboxRouter(15)), "TrigramParser")
  App.actorSystem.actorOf(Props[QuadgramParser].withRouter(SmallestMailboxRouter(15)), "QuadgramParser")

  val text = Source.fromFile(textFile).mkString.split("[\\s]{3,}")

  for (i <- (0 until text.length).par) {
    App.actor("TextUnitParser") ! TextUnitProcessor.Text(text(i), dataSet, strategy)
  }

}