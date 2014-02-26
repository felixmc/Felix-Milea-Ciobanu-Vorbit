package com.felixmilea.vorbit.actors

import akka.actor.ActorSelection
import com.felixmilea.vorbit.actors.ManagedActor.WorkCommand
import com.felixmilea.vorbit.reddit.models.RedditPost
import com.felixmilea.vorbit.reddit.models.Comment
import com.felixmilea.vorbit.data.DBConnection

class PostRecorder extends ManagedActor {
  import PostRecorder._

  private[this] lazy val db = new DBConnection(true)
  private[this] lazy val existsStatement = db.conn.prepareStatement(s"SELECT * FROM `posted_content` WHERE `parent_id`=? AND `dataset`=? AND `subset`=? AND `edition`=? LIMIT 1")
  private[this] lazy val insertStatement = db.conn.prepareStatement(s"INSERT INTO `posted_content`(`dataset`, `subset`, `edition`, `poster`, `content`, `reddit_id`, `parent_id`, `subreddit`, `date_posted`) VALUES (?,?,?,?,?,?,?,?,?)")

  def doReceive = {
    case CheckTarget(target, receiver) => {
      if (!hasPost(target.post.thingId, target.dataset, target.subset, target.edition)) {
        receiver ! target
      }
    }
    case RecordComment(target, id, content, poster) => {
      insertStatement

      insertStatement.setInt(1, target.dataset)
      insertStatement.setInt(2, target.subset)
      insertStatement.setInt(3, target.edition)
      insertStatement.setInt(4, poster)
      insertStatement.setString(5, content)
      insertStatement.setString(6, id)
      insertStatement.setString(7, target.post.thingId)
      insertStatement.setString(8, target.post.subreddit)
      insertStatement.setTimestamp(9, new java.sql.Timestamp(new java.util.Date().getTime()))

      insertStatement.executeUpdate()
      db.conn.commit()
    }
  }

  private[this] def hasPost(redditId: String, dataset: Int, subset: Int, edition: Int): Boolean = {
    existsStatement.setString(1, redditId)
    existsStatement.setInt(2, dataset)
    existsStatement.setInt(3, subset)
    existsStatement.setInt(4, edition)
    val results = existsStatement.executeQuery()
    val hasPost = results.next()
    results.close()
    return hasPost
  }

}

object PostRecorder {
  case class PostingTarget(post: RedditPost, dataset: Int, subset: Int, edition: Int)
  case class CheckTarget(target: PostingTarget, receiver: ActorSelection) extends WorkCommand
  case class RecordComment(target: PostingTarget, redditId: String, content: String, poster: Int) extends WorkCommand
}