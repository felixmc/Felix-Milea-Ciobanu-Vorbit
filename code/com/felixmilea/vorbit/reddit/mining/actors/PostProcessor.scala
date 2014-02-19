package com.felixmilea.vorbit.reddit.mining.actors

import java.sql.PreparedStatement
import com.felixmilea.vorbit.reddit.mining.actors.ManagedActor.WorkCommand
import com.felixmilea.vorbit.reddit.models.Post
import com.felixmilea.vorbit.reddit.models.Comment
import com.felixmilea.vorbit.data.DBConnection
import com.felixmilea.vorbit.reddit.models.RedditPost
import com.felixmilea.vorbit.utils.AppUtils
import com.felixmilea.vorbit.reddit.mining.actors.ManagedActor.MinerCommand
import com.felixmilea.vorbit.reddit.mining.actors.TextUnitProcessor.Text

class PostProcessor extends ManagedActor {
  import PostProcessor._

  private[this] lazy val processor = AppUtils.actor(self.path.parent.parent.child("textProcessor"))
  private[this] val db = new DBConnection(true)

  private[this] val existsStatement = db.conn.prepareStatement(s"SELECT * FROM `reddit_corpus` WHERE `reddit_id`=? AND `dataset`=? AND `subset`=? LIMIT 1")
  private[this] val insertStatement = db.conn.prepareStatement(s"INSERT INTO `reddit_corpus`(`reddit_id`, `parent`, `type`, `author`, `subreddit`, `title`, `content`, `children_count`, `ups`, `downs`, `gilded`, `date_posted`, `date_mined`, `dataset`, `subset`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)")

  def doReceive = {
    case MinerCommand(work, conf) => work match {
      case ProcessPost(post) => {
        val isPost = post.isInstanceOf[Post]
        val isNew = !hasPost(post.redditId, conf.dataset, if (isPost) "parents" else "children")

        try {
          if (isNew) {
            val ps = prepareInsert(db, post, conf.dataset, if (isPost) "parents" else "children")
            ps.executeUpdate()
            db.conn.commit()

            if (isPost) {
              processor ! Text(post.asInstanceOf[Post].title, conf.dataset, "parents")
              if (conf.task.parsePostContent) {
                processor ! Text(post.content, conf.dataset, "parents")
              }
            } else {
              processor ! Text(post.content, conf.dataset, "children")
            }
          }
        } catch {
          case t: Throwable => {
            Error(s"\tAn error was encountered while attempting to store post `$post`: ${t.getMessage}")
          }
        }
      }
    }
  }

  def hasPost(redditId: String, dataset: String, subset: String): Boolean = {
    existsStatement.setString(1, redditId)
    existsStatement.setInt(2, AppUtils.config.persistence.data.datasets(dataset))
    existsStatement.setInt(3, AppUtils.config.persistence.data.subsets(subset))
    val results = existsStatement.executeQuery()
    val hasPost = results.next()
    results.close()
    return hasPost
  }

  private def prepareInsert(db: DBConnection, post: RedditPost, dataset: String, subset: String): PreparedStatement = {
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
    insertStatement.setInt(14, AppUtils.config.persistence.data.datasets(dataset))
    insertStatement.setInt(15, AppUtils.config.persistence.data.subsets(subset))

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
  case class ProcessPost(post: RedditPost) extends WorkCommand
}