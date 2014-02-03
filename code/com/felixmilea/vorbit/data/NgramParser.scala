package com.felixmilea.vorbit.data

import akka.actor.Actor
import com.felixmilea.vorbit.analysis.TextUnitParser
import com.felixmilea.vorbit.reddit.models.Post
import com.felixmilea.vorbit.reddit.models.Comment
import com.felixmilea.vorbit.reddit.models.RedditPost
import com.mysql.jdbc.exceptions.jdbc4.MySQLSyntaxErrorException
import com.felixmilea.vorbit.utils.Loggable
import com.mysql.jdbc.MysqlDataTruncation

class NGramParser extends Actor with Loggable {
  private[this] val db = new DBConnection(true)
  private[this] val parser = new TextUnitParser
  private[this] def addStatement = db.conn.prepareCall("{CALL add_ngram(?,?,?)}")

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
    //    Debug("NgramParser received post..")
    val ids = parser.parse(text).map(ngram => {
      //      try {
      val query = addStatement
      query.clearParameters()
      query.setString(1, ngram)
      query.setString(2, dataSet)
      query.registerOutParameter(3, java.sql.Types.INTEGER)
      query.executeUpdate()
      db.conn.commit()
      val id = query.getInt(3)
      Debug(s"parsed ngram - id: $id ${" " * (3 - id.toString.length)}[ $ngram ]")
      query.close()
      Thread.sleep(1000)
      id
      //      } catch {
      //        case msqlsee: MySQLSyntaxErrorException => Error(s"Error storing ngram in database [SYNTAX]: $ngram")
      //        case msqldt: MysqlDataTruncation => Error(s"Error storing ngram in database [TRUNATE] (${ngram.length} chars): $ngram")
      //      }
    }) //.+:(NGramParser.NULL_ID).:+(NGramParser.NULL_ID)

    //    Debug("NgramParser processed post")

    //    for (i <- 0 until ids.length - 1) {
    //      // TODO: pass ngrams to bigram parser
    //    }

  }

}

object NGramParser {
  val NULL_ID = 0
  case class ParseNgrams(post: RedditPost, dataSet: String)
}