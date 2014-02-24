package com.felixmilea.vorbit.reddit.connectivity

import java.net.UnknownHostException
import scala.util.parsing.json.JSONArray
import com.felixmilea.vorbit.utils.JSON
import com.felixmilea.vorbit.utils.Log
import com.felixmilea.vorbit.utils.Loggable
import com.felixmilea.vorbit.posting.RedditUserManager

class Client(private[this] var user: RedditUser = null) extends Loggable {

  if (user != null) {
    authenticate()
  }

  def isAuthenticated = user != null && user.session != null && !user.session.isExpired

  def authenticate(): Boolean = {
    Option(user.session) match {
      case Some(sess) =>
        if (!isLoggedIn()) {
          return login()
        }

        return true
      case None => login()
    }
  }

  def authenticate(user: RedditUser): Boolean = {
    this.user = user
    this.authenticate()
  }

  def isLoggedIn(): Boolean = getJSON("api/me.json").length != 0

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
      user.session = Session.parse(conn, json("data"))
      RedditUserManager.persist(user)
    } else
      throw new AuthenticationExcetion(user.credential.username)

    return success
  }

  def get(path: String): String = {
    val conn = createConn(path)
    return checkErrors(conn)
  }

  def getJSON(path: String): JSON = JSON(get(path))

  def comment(thingId: String, text: String): String = {
    val params = new ConnectionParameters()

    params += ("api_type" -> "json")
    params += ("text" -> text)
    params += ("thing_id" -> thingId)

    val headers = getAuthHeaders()
    val conn = new Connection(s"api/comment", params, true, headers)
    val response = checkErrors(conn)

    return response
  }

  private def getAuthHeaders(): Map[String, String] = {
    if (isAuthenticated) Map(("X-Modhash", user.session.modhash), ("Cookie", s"reddit_session=${user.session.cookie}")) else Map[String, String]()
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

  private[this] def checkErrors(conn: Connection): String = {
    val response = conn.response
    val json = JSON(response).json

    if (json.has("ratelimit")) {
      throw new RateLimitException(json.ratelimit, user.credential.username)
    } else {
      for (e <- json.errors) {
        if (e(0).toString == "USER_REQUIRED") {
          throw new AuthorizationException(conn.uri)
        }
      }
    }

    return response
  }
}

class RateLimitException(val time: Double, val username: String) extends RuntimeException(s"$username has been rate limited for $time seconds.")
class AuthenticationExcetion(val username: String) extends RuntimeException(s"Authentication of client '$username' failed.")
class AuthorizationException(val path: String) extends RuntimeException(s"You must be logged in to access /$path")