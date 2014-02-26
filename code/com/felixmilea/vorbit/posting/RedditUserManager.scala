package com.felixmilea.vorbit.posting

import java.util.Date
import java.sql.PreparedStatement
import com.felixmilea.vorbit.data.DBConnection
import com.felixmilea.vorbit.reddit.connectivity.Session
import com.felixmilea.vorbit.reddit.connectivity.RedditUser
import com.felixmilea.vorbit.reddit.connectivity.Credential
import com.felixmilea.vorbit.data.ResultSetIterator
import scala.util.Random

object RedditUserManager {
  private[this] val users_table = "reddit_accounts"
  private[this] lazy val db: DBConnection = new DBConnection(true)
  private[this] lazy val insertStatement = db.conn.prepareStatement(s"INSERT INTO `$users_table`(`password`, `modhash`, `cookie`, `expiration_date`, `username`, `date_created`) VALUES (?,?,?,?,?,?)")
  private[this] lazy val updateStatement = db.conn.prepareStatement(s"UPDATE `$users_table` SET `password`=?,`modhash`=?,`cookie`=?,`expiration_date`=? WHERE `username` =?")
  private[this] lazy val selectStatement = db.conn.prepareStatement(s"SELECT * FROM `$users_table` WHERE `active`=1")

  lazy val (usersMap, userList) = {
    val users = ResultSetIterator(selectStatement.executeQuery()).map(resultSet => {
      val credential = new Credential(resultSet.getString("username"), resultSet.getString("password"))
      val session = new Session(resultSet.getString("modhash"), resultSet.getString("cookie"), resultSet.getDate("expiration_date"))
      credential.username -> new RedditUser(credential, session, resultSet.getInt("id"))
    }).toList

    (users.toMap, Random.shuffle(users.map(u => u._2)))
  }

  private[this] var nextIt = 0

  def grabNext(): RedditUser = this.synchronized {
    val next = userList(nextIt)
    nextIt = (nextIt + 1) % userList.size
    return next
  }

  def getUser(username: String): Option[RedditUser] = Option(usersMap.getOrElse(username, null))

  def addSession(username: String, session: Session) {
    val user = usersMap.getOrElse(username, null)
    if (user != null) {
      user.session = session
      persist(user)
    }
  }

  def getSession(username: String): Option[Session] = {
    val user = usersMap.getOrElse(username, null)
    if (user != null) {
      if (user.hasValidSession) Some(user.session)
      else None
    } else None
  }

  def persist(user: RedditUser) {
    val isNew = !usersMap.contains(user.credential.username)
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