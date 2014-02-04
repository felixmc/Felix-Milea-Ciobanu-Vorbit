package com.felixmilea.vorbit.data

import akka.actor.Actor
import com.felixmilea.vorbit.analysis.TextUnitParser
import com.felixmilea.vorbit.reddit.models.Post
import com.felixmilea.vorbit.reddit.models.Comment
import com.felixmilea.vorbit.reddit.models.RedditPost
import com.mysql.jdbc.exceptions.jdbc4.MySQLSyntaxErrorException
import com.felixmilea.vorbit.utils.Loggable
import com.mysql.jdbc.MysqlDataTruncation
import com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException
import com.felixmilea.vorbit.utils.ApplicationUtils

class NGramParser extends Actor with Loggable {
  private[this] val db = new DBConnection(true)
  private[this] val parser = new TextUnitParser
  private[this] val addNgramProc = db.conn.prepareCall("{CALL add_ngram(?,?,?)}")

  private def getTableB1(dataSet: String) = s"mdt_${dataSet}_b1"

  def receive = {
    case NGramParser.ParseNgrams(post, dataSet) => post match {
      case c: Comment => processNgrams(c.content, dataSet)
      case p: Post => {
        processNgrams(p.content, dataSet)
        processNgrams(p.title, dataSet)
      }
    }
  }

  private def processNgrams(text: String, dataSet: String) {
    val ids = parser.parse(text) // split text into ngrams
      .map(processNgram(_, dataSet)) // store ngrams and retrieve their id
      .filter(_ != -1) // remove bad ids
      .+:(NGramParser.NULL_ID).:+(NGramParser.NULL_ID) // padd with nulls

    for (i <- 0 until ids.length - 1) {
      ApplicationUtils.actor("BigramParser") ! BigramParser.Bigram(ids(i), ids(i + 1), dataSet)
    }

  }

  private def processNgram(ngram: String, dataSet: String): Int =
    try {
      addNgramProc.clearParameters()
      addNgramProc.setString(1, ngram)
      addNgramProc.setString(2, dataSet)
      addNgramProc.registerOutParameter(3, java.sql.Types.INTEGER)
      addNgramProc.executeUpdate()
      db.conn.commit()

      val id = addNgramProc.getInt(3)
      //      Debug(s"#$i${" " * (3 - i.toString.length)} - id: $id ${" " * (3 - id.toString.length)} [ $ngram ] $r")
      return id
    } catch {
      case msqlicve: MySQLIntegrityConstraintViolationException => {
        Error(s"Duplicate ngram: $ngram\t${msqlicve.getMessage}")
        Thread.sleep(700)
        processNgram(ngram, dataSet)
      }
      case mdt: MysqlDataTruncation => {
        Error(s"Ngram too long (${ngram.length} chars): $ngram")
        -1
      }
    }

}

object NGramParser {
  val NULL_ID = 1
  case class ParseNgrams(post: RedditPost, dataSet: String)
}