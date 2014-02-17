package com.felixmilea.vorbit.reddit.mining.actors

import com.felixmilea.vorbit.data.DBConnection
import com.felixmilea.vorbit.analysis.TextUnitParser
import com.felixmilea.vorbit.utils.AppUtils
import com.felixmilea.vorbit.utils.Loggable
import com.mysql.jdbc.MysqlDataTruncation
import com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException

class TextUnitProcessor extends ManagedActor {
  import TextUnitProcessor._

  private[this] lazy val processor = AppUtils.actor(self.path.parent.parent.child("ngramProcessor"))

  private[this] val db = new DBConnection(true)
  private[this] val parser = new TextUnitParser
  private[this] val record_1gram = db.conn.prepareCall("{CALL record_1gram(?,?,?,?,?)}")

  def doReceive = {
    case Text(text, dataset, subset) => {
      val (datasetId, subsetId, editionId) = (AppUtils.config.persistence.data.datasets(dataset),
        AppUtils.config.persistence.data.subsets(subset), AppUtils.config.persistence.data.editions(parser.edition))

      val ngrams = parser.parse(text) // split text into ngrams

      if (ngrams.length > 2) {
        val ids = ngrams.map(processNgram(_, datasetId, subsetId, editionId)) // store ngrams and retrieve their id
          .filter(_ != -1) // remove bad ids

        processor ! NgramProcessor.TextUnits(ids)
      }
    }
  }

  private def processNgram(gram: String, dataset: Int, subset: Int, edition: Int): Int =
    try {
      record_1gram.setInt(1, dataset)
      record_1gram.setInt(2, subset)
      record_1gram.setInt(3, edition)
      record_1gram.setString(4, gram)

      record_1gram.registerOutParameter(5, java.sql.Types.INTEGER)
      record_1gram.execute()
      db.conn.commit()

      return record_1gram.getInt(5)
    } catch {
      case msqlicve: MySQLIntegrityConstraintViolationException => {
        Error(s"Duplicate 1gram: $gram\t${msqlicve.getMessage}")
        Thread.sleep(500)
        processNgram(gram, dataset, subset, edition)
      }
      case mdt: MysqlDataTruncation => {
        Error(s"1gram too long (${gram.length} chars): $gram")
        return -1
      }
    }

}

object TextUnitProcessor {
  case class Text(text: String, dataset: String, subset: String)
}