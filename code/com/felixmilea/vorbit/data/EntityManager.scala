package com.felixmilea.vorbit.data

import com.felixmilea.vorbit.reddit.models.RedditPost
import com.felixmilea.vorbit.reddit.models.Comment
import com.felixmilea.vorbit.reddit.models.Post
import java.sql.Date
import com.mysql.jdbc.exceptions.MySQLIntegrityConstraintViolationException
import com.felixmilea.vorbit.utils.Log

object EntityManager {

  def insertPost(post: RedditPost, table: String) {
    val conn = mkConn()
    val ps = conn.prepareStatement(s"INSERT INTO `mined_data_$table`(`reddit_id`, `parent`, `type`, `author`, `subreddit`, `title`, `content`, `children_count`, `ups`, `downs`, `gilded`, `miner`, `date_posted`, `date_mined`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?)")

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
    ps.setInt(12, 1)
    ps.setDate(13, new Date(post.date_posted.getTime()))
    ps.setDate(14, new Date(new java.util.Date().getTime()))

    try {
      ps.executeUpdate()
      conn.commit()
    } catch {
      //      case cve: MySQLIntegrityConstraintViolationException => 
      case t: Throwable => Log.Warning(s"Attempted to insert already existing post with id `${post.redditId}`.")
    }
    conn.close()
  }

  def getAllPosts(): List[RedditPost] = {
    List()
  }

  def hasPost(redditId: String): Boolean = {
    true
  }

  private def mkConn(): DBConnection = {
    val conn = new DBConnection
    conn.connect
    return conn
  }

}