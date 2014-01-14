package com.felixmilea.vorbit.reddit.connection

import java.util.Date
import java.text.SimpleDateFormat
import com.felixmilea.vorbit.reddit.connection.Connection

class Session(_modhash: String, _cookie: String, _expiration: Date) {
  val modhash = _modhash
  val cookie = _cookie
  val expiration = _expiration
  def isExpired = new Date().after(expiration)
}

object Session {
  def parse(conn: Connection, data: Map[String, Any]): Session = {
    return new Session(
      data("modhash").asInstanceOf[String],
      data("cookie").asInstanceOf[String],
      new SimpleDateFormat("EEE, dd-MMM-yyyy HH:mm:ss zzz").parse(conn.responseHeader("Set-Cookie").split("expires=")(1).split(";")(0)))
  }
}