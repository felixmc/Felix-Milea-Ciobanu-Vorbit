package com.felixmilea.vorbit.data

import com.felixmilea.vorbit.utils.Initable
import com.felixmilea.vorbit.reddit.connectivity.Session
import com.felixmilea.vorbit.reddit.connectivity.RedditUser
import com.felixmilea.vorbit.reddit.connectivity.Credential
import java.sql.PreparedStatement
import java.util.Date

object RedditUserManager extends Initable {
  private val users = new scala.collection.mutable.HashMap[String, RedditUser]
  private var db: DBConnection = null
  private val users_table = "reddit_accounts"
  private var insertStatement: PreparedStatement = null
  private var updateStatement: PreparedStatement = null

  val dependencies = Seq(DBConfig)
  def doInit() {
    db = new DBConnection()
    db.connect

    insertStatement = db.conn.prepareStatement(s"INSERT INTO `$users_table`(`password`, `modhash`, `cookie`, `expiration_date`, `username`, `date_created`) VALUES (?,?,?,?,?,?)")
    updateStatement = db.conn.prepareStatement(s"UPDATE `$users_table` SET `password`=?,`modhash`=?,`cookie`=?,`expiration_date`=? WHERE `username` =?")

    val resultSet = db.executeQuery(s"SELECT * FROM `$users_table`")

    while (resultSet.next()) {
      val session = new Session(resultSet.getString("modhash"), resultSet.getString("modhash"), resultSet.getDate("expiration_date"))
      val credential = new Credential(resultSet.getString("username"), resultSet.getString("password"))
      val user = new RedditUser(credential, session)
      users += credential.username -> user
    }
  }

  def getUser(username: String): Option[RedditUser] = Option(users.getOrElse(username, null))
  def addUser(username: String, user: RedditUser) = {
    users += username -> user
    persist(user)
  }

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

    if (isNew) {
      ps.setDate(6, new java.sql.Date(new Date().getTime()))
    }

    ps.executeUpdate()
    db.conn.commit()
  }

}