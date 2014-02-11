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
import com.felixmilea.vorbit.utils.App
import com.felixmilea.vorbit.analysis.SymbolFriendlyStrategy
import com.felixmilea.vorbit.analysis.TextUnitParserStrategy

class TextUnitProcessor extends Actor with Loggable {
  private[this] val db = new DBConnection(true)
  private[this] val parser = new TextUnitParser
  private[this] val addNgramProc = db.conn.prepareCall("{CALL add_ngram(?,?,?)}")

  private def getTableB1(dataSet: String) = s"mdt_${dataSet}_b1"

  def receive = {
    case TextUnitProcessor.Text(text, dataSet, strategy) => processNgrams(text, dataSet, strategy)
    case TextUnitProcessor.RedditPost(post, dataSet, strategy) => post match {
      case c: Comment => processNgrams(c.content, dataSet, strategy)
      case p: Post => {
        processNgrams(p.content, dataSet, strategy)
        processNgrams(p.title, dataSet, strategy)
      }
    }
  }

  private def processNgrams(text: String, dataSet: String, strategy: TextUnitParserStrategy) {
    val parser = new TextUnitParser(strategy)
    val ids = parser.parse(text) // split text into ngrams
      .map(processNgram(_, dataSet)) // store ngrams and retrieve their id
      .filter(_ != -1) // remove bad ids
      .+:(TextUnitProcessor.NULL_ID).:+(TextUnitProcessor.NULL_ID) // padd with nulls

    // if actual ngram units were found in the text (2 means empty bc of null padding)
    if (ids.length > 3) {
      for (i <- 0 until ids.length - 1) {
        App.actor("BigramParser") ! BigramParser.Bigram(ids(i), ids(i + 1), dataSet)
      }
      for (i <- 0 until ids.length - 2) {
        App.actor("TrigramParser") ! TrigramParser.Trigram(ids(i), ids(i + 1), ids(i + 2), dataSet)
      }
      for (i <- 0 until ids.length - 3) {
        App.actor("QuadgramParser") ! QuadgramParser.Quadgram(ids(i), ids(i + 1), ids(i + 2), ids(i + 3), dataSet)
      }
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

object TextUnitProcessor {
  val NULL_ID = 1
  case class RedditPost(post: com.felixmilea.vorbit.reddit.models.RedditPost, dataSet: String, strategy: TextUnitParserStrategy = SymbolFriendlyStrategy)
  case class Text(text: String, dataSet: String, strategy: TextUnitParserStrategy = SymbolFriendlyStrategy)
}