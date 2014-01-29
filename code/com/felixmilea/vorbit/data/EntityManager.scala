package com.felixmilea.vorbit.data

import com.felixmilea.vorbit.reddit.models.RedditPost
import com.felixmilea.vorbit.reddit.models.Comment
import com.felixmilea.vorbit.reddit.models.Post
import com.mysql.jdbc.exceptions.MySQLIntegrityConstraintViolationException
import com.felixmilea.vorbit.utils.Log
import com.felixmilea.vorbit.utils.Initable
import java.sql.PreparedStatement
import com.felixmilea.vorbit.utils.Loggable

object EntityManager extends Initable with Loggable {
  val dependencies = Seq(DBConfig)
  def doInit() {
    // setup db if not already setup
    // miners table
    // accounts table
  }

  def table(miner: String) = s"mdt_${miner}_a1"

  def persistPost(post: RedditPost, miner: String): Boolean = {
    val db = getDB()
    val ps = if (hasPost(db, miner, post.redditId)) update(db, miner, post) else insert(db, miner, post)
    var success = true

    try {
      ps.executeUpdate()
      db.conn.commit()
    } catch {
      case t: Throwable => {
        Error(s"\tA database error was encountered while attempting to store post `$post`")
        success = false
      }
    } finally {
      db.conn.close()
    }

    return success
  }

  private def insert(db: DBConnection, miner: String, post: RedditPost): PreparedStatement = {
    val ps = db.conn.prepareStatement(s"INSERT INTO `${table(miner)}`(`reddit_id`, `parent`, `type`, `author`, `subreddit`, `title`, `content`, `children_count`, `ups`, `downs`, `gilded`, `miner`, `date_posted`, `date_mined`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?)")
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
    ps.setDate(12, new java.sql.Date(post.date_posted.getTime()))
    ps.setDate(13, new java.sql.Date(new java.util.Date().getTime()))

    return ps
  }
  private def update(db: DBConnection, miner: String, post: RedditPost): PreparedStatement = {
    val ps = db.conn.prepareStatement(s"UPDATE `${table(miner)}` SET `children_count`=?,`ups`=?,`downs`=?,`gilded`=?,`date_mined`=? WHERE `reddit_id`=?")
    val isComment = post.isInstanceOf[Comment]

    ps.setInt(1, post.children_count)
    ps.setInt(2, post.ups)
    ps.setInt(3, post.downs)
    ps.setInt(4, if (isComment) post.asInstanceOf[Comment].gilded else 0)
    ps.setDate(5, new java.sql.Date(post.date_posted.getTime()))
    ps.setString(6, post.redditId)

    return ps
  }

  def hasPost(db: DBConnection, miner: String, redditId: String): Boolean = db.executeQuery(s"SELECT * FROM `${table(miner)}` WHERE `reddit_id` = '$redditId' LIMIT 1").next()

  def setupMiner(name: String) {
    val db = getDB()
    val cs = db.conn.prepareCall("call setup_miner(?)")
    cs.setString(1, name)
    cs.executeUpdate()
    db.conn.commit()
    db.conn.close()
  }

  private def getDB(): DBConnection = {
    val conn = new DBConnection
    conn.connect
    return conn
  }

}