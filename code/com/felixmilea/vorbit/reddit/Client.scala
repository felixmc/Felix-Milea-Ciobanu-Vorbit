package com.felixmilea.vorbit.reddit

class Client {
  var session: Session = null

  def isAuthenticated = session != null && !session.isExpired

  def authenticate(cred: Credential): Boolean = SessionManager.findSession(cred.username) match {
    case Some(sess) =>
      session = sess
      println("using cached session")
      return true
    case None =>
      println("using fresh session")

      val params = new ConnectionParameters

      params += "user" -> cred.username
      params += "passwd" -> cred.password
      params += "rem" -> "true"

      val conn = new Connection(Nodes.login, params, true)
      val data = JParser.parse(conn.response, JParser.Strategy.login)(0)
      val errors = data("errors").asInstanceOf[List[List[String]]]

      val success = errors.length == 0

      if (success) {
        session = Session.parse(conn, data)
        SessionManager.addSession(cred.username, session)
      } else
        clientError(s"VorbitBot authentication failed for user [${cred.username}] with password [${cred.password}]", errors)

      return success
  }

  def getHome(): String = {
    val conn = createConn(Nodes.me)
    return conn.response
  }

  private def createConn(path: String): Connection = {
    val sessionHeaders = if (session != null) Map(("X-Modhash", session.modhash), ("Cookie", s"reddit_session=${session.cookie}")) else Map[String, String]()
    return new Connection(path, headers = sessionHeaders)
  }

  def clientError(header: String, errors: List[List[String]]) {
    println(s"header:")
    for (err <- errors) {
      println(s"   - ${err(0)}: ${err(1)}")
    }
  }
}