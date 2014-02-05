package com.felixmilea.vorbit.data

import akka.actor.Actor

class BigramParser extends Actor {
  private[this] lazy val db = new DBConnection(true)
  private[this] val addBigramsProc = db.conn.prepareCall("{CALL add_bigrams(?,?,?)}")

  def receive = {
    case BigramParser.Bigram(a, b, dataSet) => {
      addBigramsProc.clearParameters()
      addBigramsProc.setInt(1, a)
      addBigramsProc.setInt(2, b)
      addBigramsProc.setString(3, dataSet)
      addBigramsProc.executeUpdate()
      db.conn.commit()
    }
  }

}

object BigramParser {
  case class Bigram(ngram1: Int, ngram2: Int, dataSet: String)
}