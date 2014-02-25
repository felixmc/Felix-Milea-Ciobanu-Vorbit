package com.felixmilea.vorbit.actors

import com.mysql.jdbc.exceptions.jdbc4.MySQLTransactionRollbackException
import com.felixmilea.vorbit.data.DBConnection

class NgramProcessor extends ManagedActor {
  import NgramProcessor._

  private[this] lazy val db = new DBConnection(true)
  private[this] lazy val addBigramsProc = db.conn.prepareCall("{CALL record_ngram(?,?)}")

  def doReceive = {
    case TextUnits(units) => {
      for (u <- 0 until units.length) {
        for (n <- minLevel to maxLevel) {
          if (u + n <= units.length) {
            var successful = false

            do {
              try {
                addBigramsProc.setInt(1, n)
                addBigramsProc.setString(2, units.slice(u, u + n).mkString(","))
                addBigramsProc.execute()
                db.conn.commit()
                successful = true
              } catch {
                case tre: MySQLTransactionRollbackException => {
                  Warning("insert deadlock..will try again in 1000ms")
                  successful = false
                  Thread.sleep(1000)
                }
              }
            } while (!successful)
          }
        }
      }
    }
  }

}

object NgramProcessor {
  val minLevel = 2
  val maxLevel = 4
  case class TextUnits(units: Seq[Int])
}