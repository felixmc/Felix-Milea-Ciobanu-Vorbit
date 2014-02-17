package com.felixmilea.vorbit.reddit.connectivity

import java.net.UnknownHostException
import scala.util.parsing.json.JSONArray
import com.felixmilea.vorbit.utils.JSON
import com.felixmilea.vorbit.utils.Log
import com.felixmilea.vorbit.data.RedditUserManager
import com.felixmilea.vorbit.utils.Loggable

class Client extends Loggable {
  var session: Session = null

  def isAuthenticated = session != null && !session.isExpired

  def authenticate(user: RedditUser): Boolean = RedditUserManager.getSession(user.credential.username) match {
    case Some(sess) =>
      session = sess
      return true
    case None =>
      val params = new ConnectionParameters

      params ++= Seq(("user" -> user.credential.username), ("passwd" -> user.credential.password), ("rem" -> "true"))

      val conn = try {
        new Connection(Nodes.login, params, true)
      } catch {
        case uhe: UnknownHostException =>
          Error("VorbitBot encountered an error while connecting to host: " + uhe)
          null
      }

      val json = JSON(conn.response)("json")

      val errors = json("errors")
      val success = errors.length == 0

      if (success) {
        session = Session.parse(conn, json("data"))
        user.session = session
        RedditUserManager.persist(user)
      } else
        clientError(s"VorbitBot authentication failed for client with username `${user.credential.username}`", errors)

      return success
  }

  def get(path: String): String = {
    val conn = createConn(path)
    return conn.response
  }

  def getJSON(path: String): JSON = JSON(get(path))

  private def createConn(path: String): Connection = {
    val sessionHeaders = if (session != null) Map(("X-Modhash", session.modhash), ("Cookie", s"reddit_session=${session.cookie}")) else Map[String, String]()

    try {
      new Connection(path, headers = sessionHeaders)
    } catch {
      case uhe: UnknownHostException =>
        Error("VorbitBot encountered an error while connecting to host: " + uhe)
        null
    }

  }

  def clientError(header: String, errors: JSON) {
    Error(s"$header:")
    for (errorIndex <- 0 until errors.length) {
      Error(s"\t- ${errors(errorIndex)(0)}: ${errors(1)}")
    }
  }
}