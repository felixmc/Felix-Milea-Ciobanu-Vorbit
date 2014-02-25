package com.felixmilea.vorbit.actors

import java.sql.CallableStatement
import java.sql.SQLException
import com.mysql.jdbc.MysqlDataTruncation
import com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException
import com.mysql.jdbc.exceptions.jdbc4.MySQLTransactionRollbackException
import akka.actor.ActorSelection
import com.felixmilea.vorbit.actors.ManagedActor.WorkCommand
import com.felixmilea.vorbit.reddit.mining.RedditMiningManager
import com.felixmilea.vorbit.data.DBConnection
import com.felixmilea.vorbit.analysis.TextUnitParser
import com.felixmilea.vorbit.utils.AppUtils

class TextUnitProcessor extends ManagedActor {
  import TextUnitProcessor._

  private[this] lazy val processor = sibling(RedditMiningManager.Names.ngram)

  private[this] val db = new DBConnection(true)
  private[this] lazy val recordSt = db.conn.prepareCall("{CALL record_1gram(?,?,?,?,?)}")
  private[this] lazy val retrieveSt = db.conn.prepareCall("{CALL retrieve_1gram(?,?,?,?,?)}")

  private[this] val parser = new TextUnitParser
  private[this] val editionId = AppUtils.config.persistence.data.editions(parser.edition)

  def doReceive = {
    case RecordText(text, dataset, subset) => {
      ifSome(getNgrams(text, dataset, subset, true)) { processor ! NgramProcessor.TextUnits(_) }
    }
    case RetrieveText(text, dataset, subset, id, receiver) => {
      ifSome(getNgrams(text, dataset, subset, false)) { receiver ! GramSet(dataset, editionId, subset, id, _) }
    }
  }

  private[this] def ifSome[T](dataOption: Option[T])(callback: Function[T, Unit]) = dataOption match {
    case Some(data) => callback(data)
    case None => {}
  }

  private[this] def getNgrams(text: String, dataset: Int, subset: Int, record: Boolean): Option[Seq[Int]] = {
    val ngrams = parser.parse(text) // split text into ngrams

    if (ngrams.length > 2) {
      val ids = ngrams.map(processNgram(if (record) recordSt else retrieveSt, _, dataset, subset)) // store ngrams and retrieve their id
        .filter(_ != -1) // remove bad ids
      return Some(ids)
    } else
      return None
  }

  private[this] def processNgram(statement: CallableStatement, gram: String, dataset: Int, subset: Int): Int =
    try {
      statement.setInt(1, dataset)
      statement.setInt(2, subset)
      statement.setInt(3, editionId)
      statement.setString(4, gram)

      statement.registerOutParameter(5, java.sql.Types.INTEGER)
      statement.execute()
      db.conn.commit()

      return statement.getInt(5)
    } catch {
      case icve: MySQLIntegrityConstraintViolationException => {
        Error(s"Duplicate 1gram: $gram\t${icve.getMessage}")
        Thread.sleep(500)
        processNgram(statement, gram, dataset, subset)
      }
      case tre: MySQLTransactionRollbackException => {
        Warning("insert deadlock..will try again in 1000ms")
        Thread.sleep(1000)
        processNgram(statement, gram, dataset, subset)
      }
      case mdt: MysqlDataTruncation => {
        Error(s"1gram too long (${gram.length} chars): $gram")
        return -1
      }
      case sqle: SQLException => {
        Error("SQL error occurred..will try again in 1000ms: " + sqle.getMessage)
        Thread.sleep(1000)
        processNgram(statement, gram, dataset, subset)
      }
    }

}

object TextUnitProcessor {
  case class RecordText(text: String, dataset: Int, subset: Int) extends WorkCommand
  case class RetrieveText(text: String, dataset: Int, subset: Int, id: String, receiver: ActorSelection) extends WorkCommand
  case class GramSet(dataset: Int, edition: Int, subset: Int, id: String, data: Seq[Int])
}