package com.felixmilea.vorbit.reddit.connectivity

import java.util.Date
import java.text.SimpleDateFormat
import com.felixmilea.vorbit.utils.JSON

class Session(val modhash: String, val cookie: String, val expiration: Date) {
  def isExpired = expiration == null || new Date().after(expiration)
  def isValid = modhash != null && cookie != null && !isExpired
}

object Session {
  def parse(conn: Connection, data: JSON): Session = {
    return new Session(
      data("modhash"),
      data("cookie"),
      new SimpleDateFormat("EEE, dd-MMM-yyyy HH:mm:ss zzz").parse(conn.responseHeader("Set-Cookie").split("expires=")(1).split(";")(0)))
  }
}