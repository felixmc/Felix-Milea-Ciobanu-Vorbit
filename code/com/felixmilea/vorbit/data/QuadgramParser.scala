package com.felixmilea.vorbit.data

import akka.actor.Actor

class QuadgramParser extends Actor {
  private[this] lazy val db = new DBConnection(true)
  private[this] val addBigramsProc = db.conn.prepareCall("{CALL add_quadgrams(?,?,?,?,?)}")

  def receive = {
    case QuadgramParser.Quadgram(a, b, c, d, dataSet) => {
      addBigramsProc.clearParameters()
      addBigramsProc.setInt(1, a)
      addBigramsProc.setInt(2, b)
      addBigramsProc.setInt(3, c)
      addBigramsProc.setInt(4, d)
      addBigramsProc.setString(5, dataSet)
      addBigramsProc.executeUpdate()
      db.conn.commit()
    }
  }

}

object QuadgramParser {
  case class Quadgram(ngram1: Int, ngram2: Int, ngram3: Int, ngram4: Int, dataSet: String)
}