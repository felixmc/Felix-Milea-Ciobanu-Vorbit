package com.felixmilea.vorbit.data

import java.sql.PreparedStatement
import akka.actor.Actor
import com.felixmilea.vorbit.reddit.models.RedditPost
import com.felixmilea.vorbit.reddit.models.Comment
import com.felixmilea.vorbit.utils.Loggable
import com.felixmilea.vorbit.reddit.models.Post
import com.felixmilea.vorbit.utils.ApplicationUtils

class DataSetManager extends Actor with Loggable {
  private[this] lazy val db = new DBConnection(true)
  private[this] lazy val setupStatement = db.conn.prepareCall("CALL setup_dataset(?)")

  private def getTableA(dataSet: String) = s"mdt_${dataSet}_a1"

  private def persist(post: RedditPost, dataSet: String) {
    val ps = if (hasPost(db, dataSet, post.redditId)) update(db, dataSet, post) else insert(db, dataSet, post)

    try {
      ps.executeUpdate()
      db.conn.commit()
      ApplicationUtils.actor("NgramParser") ! NGramParser.ParseNgrams(post, dataSet)
    } catch {
      case t: Throwable => {
        Error(s"\tAn error was encountered while attempting to store post `$post`: ${t.getMessage}")
      }
    }
  }

  private def insert(db: DBConnection, dataSet: String, post: RedditPost): PreparedStatement = {
    val ps = db.conn.prepareStatement(s"INSERT INTO `${getTableA(dataSet)}`(`reddit_id`, `parent`, `type`, `author`, `subreddit`, `title`, `content`, `children_count`, `ups`, `downs`, `gilded`, `date_posted`, `date_mined`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)")
    val isComment = post.isInstanceOf[Comment]

    ps.setString(1, post.redditId)
    ps.setString(2, if (isComment) post.asInstanceOf[Comment].parentId else null)
    ps.setString(3, if (isComment) "t1" else "t3")
    ps.setString(4, post.author)
    ps.setString(5, post.subreddit)
    ps.setString(6, if (isComment) null else post.asInstanceOf[Post].title)
    ps.setString(7, post.content)
    ps.setInt(8, post.children_count)
    ps.setInt(9, post.ups)
    ps.setInt(10, post.downs)
    ps.setInt(11, if (isComment) post.asInstanceOf[Comment].gilded else 0)
    ps.setTimestamp(12, new java.sql.Timestamp(post.date_posted.getTime()))
    ps.setTimestamp(13, new java.sql.Timestamp(new java.util.Date().getTime()))

    return ps
  }
  private def update(db: DBConnection, dataSet: String, post: RedditPost): PreparedStatement = {
    val ps = db.conn.prepareStatement(s"UPDATE `${getTableA(dataSet)}` SET `children_count`=?,`ups`=?,`downs`=?,`gilded`=?,`date_mined`=? WHERE `reddit_id`=?")
    val isComment = post.isInstanceOf[Comment]

    ps.setInt(1, post.children_count)
    ps.setInt(2, post.ups)
    ps.setInt(3, post.downs)
    ps.setInt(4, if (isComment) post.asInstanceOf[Comment].gilded else 0)
    ps.setTimestamp(5, new java.sql.Timestamp(post.date_posted.getTime()))
    ps.setString(6, post.redditId)

    return ps
  }

  def hasPost(db: DBConnection, dataSet: String, redditId: String): Boolean = db.executeQuery(s"SELECT * FROM `${getTableA(dataSet)}` WHERE `reddit_id` = '$redditId' LIMIT 1").next()

  def setupDataSet(name: String) {
    setupStatement.setString(1, name)
    setupStatement.executeUpdate()
    db.conn.commit()
  }

  import DataSetManager._

  def receive = {
    case PersistPost(post, dateSet) => {
      persist(post, dateSet)
    }
  }

}

object DataSetManager {
  case class PersistPost(post: RedditPost, dataSet: String)
  case class SetupDataSet(name: String)
}