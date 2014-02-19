package com.felixmilea.vorbit.reddit.connectivity

import java.net.UnknownHostException
import scala.util.parsing.json.JSONArray
import com.felixmilea.vorbit.utils.JSON
import com.felixmilea.vorbit.utils.Log
import com.felixmilea.vorbit.data.RedditUserManager
import com.felixmilea.vorbit.utils.Loggable

class Client extends Loggable {
  var session: Session = null
  var user: RedditUser = null

  def isAuthenticated = session != null && !session.isExpired

  def authenticate(user: RedditUser): Boolean = {
    this.user = user

    RedditUserManager.getSession(user.credential.username) match {
      case Some(sess) =>
        session = sess

        val me = getJSON("api/me.json")

        if (me.length == 0) {
          return login()
        }

        return true
      case None => {
        login()
      }
    }
  }

  private[this] def login(): Boolean = {
    val params = new ConnectionParameters

    params ++= Seq(("user" -> user.credential.username), ("passwd" -> user.credential.password), ("rem" -> "true"))

    val conn = try {
      new Connection(Nodes.login, params, true)
    } catch {
      case uhe: UnknownHostException =>
        Error("VorbitBot encountered an error while connecting to host: " + uhe)
        null
    }

    val json = JSON(conn.response).json

    val errors = json.errors
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

  def comment(thingId: String, text: String): String = {
    val params = new ConnectionParameters()

    params += ("api_type" -> "json")
    params += ("text" -> text)
    params += ("thing_id" -> thingId)

    val headers = getAuthHeaders()
    val conn = new Connection(s"api/comment", params, true, headers)
    val response = conn.response

    return conn.response
  }

  private def getAuthHeaders(): Map[String, String] = {
    if (session != null) Map(("X-Modhash", session.modhash), ("Cookie", s"reddit_session=${session.cookie}")) else Map[String, String]()
  }

  private def createConn(path: String, post: Boolean = false): Connection = {
    val sessionHeaders = getAuthHeaders()

    return try {
      new Connection(path, isPost = post, headers = sessionHeaders)
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