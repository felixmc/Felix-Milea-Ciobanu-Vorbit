package com.felixmilea.vorbit.reddit.connectivity

import java.net.UnknownHostException
import scala.util.parsing.json.JSONArray
import com.felixmilea.vorbit.utils.JSON
import com.felixmilea.vorbit.utils.Log
import com.felixmilea.vorbit.utils.Loggable
import com.felixmilea.vorbit.posting.RedditUserManager

class Client(private[this] var user: RedditUser = null) extends Loggable {

  if (user != null) authenticate()

  private[this] var meCache: JSON = null

  def me: JSON = {
    meCache = getJSON("api/me.json")
    return meCache
  }

  def hasMail: Boolean = if (meCache == null) me.data.has_mail else meCache.data.has_mail
  def commentKarma: Int = if (meCache == null) me.data.comment_karma else meCache.data.comment_karma

  def getUser: RedditUser = user
  def hasValidCredentials = user != null && user.session != null && !user.session.isExpired
  def isLoggedIn(): Boolean = hasValidCredentials && me.length != 0

  def authenticate(): Boolean = tryLogin()
  def authenticate(user: RedditUser): Boolean = {
    this.user = user
    this.authenticate()
  }

  private[this] def tryLogin(): Boolean = return if (!isLoggedIn()) login() else true

  private[this] def login(): Boolean = {
    val params = new ConnectionParameters
    params ++= Seq(("user" -> user.credential.username), ("passwd" -> user.credential.password), ("rem" -> "true"))

    val conn = new Connection(Nodes.login, params, true)

    val response = checkErrors(conn)
    val json = JSON(response).json

    val success = json.errors.length == 0

    if (success) {
      user.session = Session.parse(conn, json("data"))
      RedditUserManager.persist(user)
      return isLoggedIn()
    } else {
      Error(response)
      throw new AuthenticationExcetion(user.credential.username)
    }
  }

  def get(path: String): String = {
    val conn = createConn(path)
    return checkErrors(conn)
  }

  def getJSON(path: String): JSON = JSON(get(path))

  def comment(thingId: String, text: String): String = {
    tryLogin()

    val params = new ConnectionParameters()
    params += ("api_type" -> "json")
    params += ("text" -> text)
    params += ("thing_id" -> thingId)

    val headers = getAuthHeaders()
    val conn = new Connection(s"api/comment", params, true, headers)
    val response = checkErrors(conn)

    return response
  }

  private[this] def getAuthHeaders(): Map[String, String] =
    if (hasValidCredentials) Map(("X-Modhash", user.session.modhash), ("Cookie", s"reddit_session=${user.session.cookie}"))
    else Map[String, String]()

  private[this] def createConn(path: String, post: Boolean = false): Connection = new Connection(path, isPost = post, headers = getAuthHeaders())

  private[this] def checkErrors(conn: Connection, needsAuth: Boolean = false): String = {
    val response = conn.response
    val jsonraw = JSON(response)
    val json = if (jsonraw.has("json")) jsonraw.json else jsonraw

    if (json.has("ratelimit")) {
      throw new RateLimitException(user.credential.username, json.ratelimit)
    } else if (json.has("errors")) {
      for (e <- json.errors) {
        val header = e(0).toString
        lazy val message = e(1).toString

        header match {
          case "RATELIMIT" => {
            val mins = message.split("try again in ")(1).split(" ")(0).toInt
            throw new RateLimitException(user.credential.username, mins * 60)
          }
          case "TOO_OLD" => throw new TooOldException(conn.uri)
          case "USER_REQUIRED" => throw new AuthorizationException(conn.uri)
          case "WRONG_PASSWORD" => throw new WrongPassword(user.credential)
          case "DELETED_LINK" => throw new DeletedLinkException(conn.uri)
          case _ => Warning(s"Unexpected Reddit Client Error: '$header: $message'")
        }
      }
    }

    return response
  }
}

class RateLimitException(val username: String, val time: Double) extends RedditClientException(s"$username has been rate limited for $time seconds.")
class AuthenticationExcetion(val username: String, cause: String = "unknown cause") extends RedditClientException(s"Authentication of client with user '$username' failed due to $cause.")
class WrongPassword(cred: Credential) extends AuthenticationExcetion(cred.username, s"bad password '${cred.password}'")
class AuthorizationException(val path: String) extends RedditClientException(s"You must be logged in to access /$path")
class TooOldException(val path: String) extends RedditClientException(s"The following content is too old to interact with: /$path")
class NoTextException(val path: String) extends RedditClientException(s"Need text to post to: /$path")
class DeletedLinkException(val path: String) extends RedditClientException(s"The following link has been deleted: /$path")
class RedditClientException(message: String) extends RuntimeException(message)