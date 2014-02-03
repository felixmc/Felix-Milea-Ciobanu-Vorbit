package com.felixmilea.vorbit.data

import akka.actor.Actor

class BigramParser extends Actor {
  private[this] lazy val db = new DBConnection(true)

  private def getTableC1(dataSet: String) = s"mdt_${dataSet}_c1"

  def receive = {
    case (a: Int, b: Int) => {

    }
  }

}

object BigramParser {
  case class ParseBigrams(ngrams: Array[String], dataSet: String)
}