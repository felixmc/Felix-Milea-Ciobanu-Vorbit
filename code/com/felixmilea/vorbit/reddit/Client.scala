package com.felixmilea.vorbit.reddit

class Client {
  var modhash: String = null
  var cookie: String = null

  def authenticate(cred: Credential): Boolean = {
    val params = new ConnectionParameters

    params += "user" -> cred.username
    params += "passwd" -> cred.password
    params += "rem" -> "true"

    val conn = new Connection(Nodes.login, params)
    val data = JParser.parse(conn.response, JParser.Strategy.login)(0)
    val errors = data("errors").asInstanceOf[List[List[String]]]

    val success = errors.length == 0

    if (success) {
      modhash = data("modhash").asInstanceOf[String]
      cookie = data("cookie").asInstanceOf[String]
    } else {
      println(s"VorbitBot authentication failed for user [${cred.username}] with password [${cred.password}]:")
      for (err <- errors) {
        println(s"   - ${err(0)}: ${err(1)}")
      }
    }

    return success
  }
}