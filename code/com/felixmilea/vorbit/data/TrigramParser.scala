package com.felixmilea.vorbit.data

import akka.actor.Actor

class TrigramParser extends Actor {
  private[this] lazy val db = new DBConnection(true)
  private[this] val addBigramsProc = db.conn.prepareCall("{CALL add_trigrams(?,?,?,?)}")

  def receive = {
    case TrigramParser.Trigram(a, b, c, dataSet) => {
      addBigramsProc.clearParameters()
      addBigramsProc.setInt(1, a)
      addBigramsProc.setInt(2, b)
      addBigramsProc.setInt(3, c)
      addBigramsProc.setString(4, dataSet)
      addBigramsProc.executeUpdate()
      db.conn.commit()
    }
  }

}

object TrigramParser {
  case class Trigram(ngram1: Int, ngram2: Int, ngram3: Int, dataSet: String)
}