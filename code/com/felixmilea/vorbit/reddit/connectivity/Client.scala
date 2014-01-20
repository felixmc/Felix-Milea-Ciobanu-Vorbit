package com.felixmilea.vorbit.reddit.connectivity

import com.felixmilea.vorbit.JSON.JSONParser
import com.felixmilea.vorbit.utils.Log
import scala.util.parsing.json.JSONArray
import com.felixmilea.vorbit.JSON.JSONTraverser
import com.felixmilea.vorbit.JSON.JSONTraverser
import com.felixmilea.vorbit.data.RedditUserManager

class Client {
  var session: Session = null

  def isAuthenticated = session != null && !session.isExpired

  def authenticate(user: RedditUser): Boolean = RedditUserManager.getSession(user.credential.username) match {
    case Some(sess) =>
      session = sess
      return true
    case None =>
      val params = new ConnectionParameters

      params ++= Seq(("user" -> user.credential.username), ("passwd" -> user.credential.password), ("rem" -> "true"))

      val conn = new Connection(Nodes.login, params, true)
      val json = JSONParser.parse(conn.response)("json")

      val errors = json("errors")
      val success = errors(JSONParser.L).get.length == 0

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

  def getJSON(path: String): JSONTraverser = return JSONParser.parse(get(path + ".json"))

  private def createConn(path: String): Connection = {
    val sessionHeaders = if (session != null) Map(("X-Modhash", session.modhash), ("Cookie", s"reddit_session=${session.cookie}")) else Map[String, String]()
    return new Connection(path, headers = sessionHeaders)
  }

  def clientError(header: String, errors: JSONTraverser) {
    Log.Error(s"$header:")
    for (errorIndex <- 0 until errors(JSONParser.L).get.length) {
      val err = errors(errorIndex)(JSONParser.L).get.asInstanceOf[List[String]]
      Log.Error(s"\t- ${err(0)}: ${err(1)}")
    }
  }
}