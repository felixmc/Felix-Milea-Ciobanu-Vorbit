package com.felixmilea.vorbit.actors

import java.sql.PreparedStatement
import com.felixmilea.vorbit.actors.ManagedActor.WorkCommand
import com.felixmilea.vorbit.reddit.models.Post
import com.felixmilea.vorbit.reddit.models.Comment
import com.felixmilea.vorbit.data.DBConnection
import com.felixmilea.vorbit.reddit.models.RedditPost
import com.felixmilea.vorbit.actors.TextUnitProcessor.RecordText
import com.felixmilea.vorbit.utils.AppUtils
import akka.actor.ActorSelection

class PostProcessor extends ManagedActor {
  import PostProcessor._

  private[this] val db = new DBConnection(true)

  private[this] val existsStatement = db.conn.prepareStatement(s"SELECT * FROM `reddit_corpus` WHERE `reddit_id`=? AND `dataset`=? AND `subset`=? LIMIT 1")
  private[this] val insertStatement = db.conn.prepareStatement(s"INSERT INTO `reddit_corpus`(`reddit_id`, `parent`, `type`, `author`, `subreddit`, `title`, `content`, `children_count`, `ups`, `downs`, `gilded`, `date_posted`, `date_mined`, `dataset`, `subset`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)")

  private[this] val (parentsSubset, childrenSubset) = (AppUtils.config.persistence.data.subsets("parents"), AppUtils.config.persistence.data.subsets("children"))

  def doReceive = {
    case ProcessPost(post, dataset) => {
      val isPost = post.isInstanceOf[Post]
      val subset = if (isPost) parentsSubset else childrenSubset
      val isNew = !hasPost(post.redditId, dataset, subset)

      try {
        if (isNew) {
          val ps = prepareInsert(db, post, dataset, subset)
          ps.executeUpdate()
          db.conn.commit()
        }
      } catch {
        case t: Throwable => {
          Error(s"\tAn error was encountered while attempting to store post `$post`: ${t.getMessage}")
        }
      }
    }
  }

  def hasPost(redditId: String, dataset: Int, subset: Int): Boolean = {
    existsStatement.setString(1, redditId)
    existsStatement.setInt(2, dataset)
    existsStatement.setInt(3, subset)
    val results = existsStatement.executeQuery()
    val hasPost = results.next()
    results.close()
    return hasPost
  }

  private def prepareInsert(db: DBConnection, post: RedditPost, dataset: Int, subset: Int): PreparedStatement = {
    val isComment = post.isInstanceOf[Comment]

    insertStatement.setString(1, post.redditId)
    insertStatement.setString(2, if (isComment) post.asInstanceOf[Comment].parentId else null)
    insertStatement.setString(3, if (isComment) "t1" else "t3")
    insertStatement.setString(4, post.author)
    insertStatement.setString(5, post.subreddit)
    insertStatement.setString(6, if (isComment) null else post.asInstanceOf[Post].title)
    insertStatement.setString(7, post.content)
    insertStatement.setInt(8, post.children_count)
    insertStatement.setInt(9, post.ups)
    insertStatement.setInt(10, post.downs)
    insertStatement.setInt(11, if (isComment) post.asInstanceOf[Comment].gilded else 0)
    insertStatement.setTimestamp(12, new java.sql.Timestamp(post.date_posted.getTime()))
    insertStatement.setTimestamp(13, new java.sql.Timestamp(new java.util.Date().getTime()))
    insertStatement.setInt(14, dataset)
    insertStatement.setInt(15, subset)

    return insertStatement
  }

  private def update(db: DBConnection, dataSet: String, post: RedditPost): PreparedStatement = {
    val ps = db.conn.prepareStatement(s"UPDATE `reddit_corpus` SET `children_count`=?,`ups`=?,`downs`=?,`gilded`=?,`date_mined`=? WHERE `reddit_id`=?")
    val isComment = post.isInstanceOf[Comment]

    ps.setInt(1, post.children_count)
    ps.setInt(2, post.ups)
    ps.setInt(3, post.downs)
    ps.setInt(4, if (isComment) post.asInstanceOf[Comment].gilded else 0)
    ps.setTimestamp(5, new java.sql.Timestamp(post.date_posted.getTime()))
    ps.setString(6, post.redditId)

    return ps
  }

}

object PostProcessor {
  case class ProcessPost(post: RedditPost, dataset: Int) extends WorkCommand
}