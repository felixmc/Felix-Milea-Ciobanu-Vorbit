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
  private[this] lazy val db = new DBConnection(true)

  def doReceive = {
    case MinerCommand(work, conf) => work match {
      case ProcessPost(post) => {
        val isNew = !hasPost(conf.dataset, post.redditId)
        val isPost = post.isInstanceOf[Post]
        //    val ps = if (isOld) update(db, dataSet, post) else insert(db, dataSet, post)

        try {
          if (isNew) {
            val ps = insert(db, conf.dataset, post)
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

  def hasPost(dataSet: String, redditId: String): Boolean = {
    val rs = db.executeQuery(s"SELECT * FROM `${table(dataSet)}` WHERE `reddit_id` = '$redditId' LIMIT 1")
    val hasPost = rs.next()
    rs.close()
    return hasPost
  }

  private def insert(db: DBConnection, dataSet: String, post: RedditPost): PreparedStatement = {
    val ps = db.conn.prepareStatement(s"INSERT INTO `${table(dataSet)}`(`reddit_id`, `parent`, `type`, `author`, `subreddit`, `title`, `content`, `children_count`, `ups`, `downs`, `gilded`, `date_posted`, `date_mined`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)")
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
    val ps = db.conn.prepareStatement(s"UPDATE `${table(dataSet)}` SET `children_count`=?,`ups`=?,`downs`=?,`gilded`=?,`date_mined`=? WHERE `reddit_id`=?")
    val isComment = post.isInstanceOf[Comment]

    ps.setInt(1, post.children_count)
    ps.setInt(2, post.ups)
    ps.setInt(3, post.downs)
    ps.setInt(4, if (isComment) post.asInstanceOf[Comment].gilded else 0)
    ps.setTimestamp(5, new java.sql.Timestamp(post.date_posted.getTime()))
    ps.setString(6, post.redditId)

    return ps
  }

  def table(dataset: String) = s"reddit_corpus_$dataset"

}

object PostProcessor {
  case class ProcessPost(post: RedditPost) extends WorkCommand
  //  case class ProcessComment(comment: Comment) extends WorkCommand
}