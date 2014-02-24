package com.felixmilea.vorbit.posting

import java.util.Date
import java.sql.PreparedStatement
import com.felixmilea.vorbit.data.DBConnection
import com.felixmilea.vorbit.reddit.connectivity.Session
import com.felixmilea.vorbit.reddit.connectivity.RedditUser
import com.felixmilea.vorbit.reddit.connectivity.Credential
import com.felixmilea.vorbit.data.ResultSetIterator

object RedditUserManager {
  private[this] val users_table = "reddit_accounts"
  private[this] lazy val db: DBConnection = new DBConnection(true)
  private[this] lazy val insertStatement = db.conn.prepareStatement(s"INSERT INTO `$users_table`(`password`, `modhash`, `cookie`, `expiration_date`, `username`, `date_created`) VALUES (?,?,?,?,?,?)")
  private[this] lazy val updateStatement = db.conn.prepareStatement(s"UPDATE `$users_table` SET `password`=?,`modhash`=?,`cookie`=?,`expiration_date`=? WHERE `username` =?")
  private[this] lazy val selectStatement = db.conn.prepareStatement(s"SELECT * FROM `$users_table`")

  lazy val users = {
    ResultSetIterator(selectStatement.executeQuery()).map(resultSet => {
      val credential = new Credential(resultSet.getString("username"), resultSet.getString("password"))
      val session = new Session(resultSet.getString("modhash"), resultSet.getString("modhash"), resultSet.getDate("expiration_date"))
      credential.username -> new RedditUser(credential, session)
    }).toMap
  }

  private[this] var nextIt = 0

  def grabNext(): RedditUser = {
    val next = users(users.keys.toList(nextIt))
    nextIt = (nextIt + 1) % users.size
    return next
  }

  def getUser(username: String): Option[RedditUser] = Option(users.getOrElse(username, null))

  def addSession(username: String, session: Session) {
    val user = users.getOrElse(username, null)
    if (user != null) {
      user.session = session
      persist(user)
    }
  }

  def getSession(username: String): Option[Session] = {
    val user = users.getOrElse(username, null)
    if (user != null) {
      if (user.hasValidSession) Some(user.session)
      else None
    } else None
  }

  def persist(user: RedditUser) {
    val isNew = !users.contains(user.credential.username)
    val ps = if (isNew) insertStatement else updateStatement

    ps.setString(1, user.credential.password)
    ps.setString(2, user.session.modhash)
    ps.setString(3, user.session.cookie)
    ps.setDate(4, new java.sql.Date(user.session.expiration.getTime))
    ps.setString(5, user.credential.username)

    if (isNew)
      ps.setDate(6, new java.sql.Date(new Date().getTime()))

    ps.executeUpdate()
    db.conn.commit()
  }

}