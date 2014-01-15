package com.felixmilea.vorbit.managers

import scala.io.Source
import com.felixmilea.vorbit.reddit.connectivity.Session

object SessionManager {
  private val sessionsFile = "appdata/sessions.data"
  private var sessions: scala.collection.mutable.Map[String, Session] = null

  def init() {
    sessions = new scala.collection.mutable.HashMap[String, Session]
    if (new java.io.File(sessionsFile).exists)
      Source.fromFile(sessionsFile).getLines.foreach(line =>
        if (!line.isEmpty()) {
          val parts = line.split("/")
          val session = new Session(parts(1), parts(2), new java.util.Date(parts(3).toLong))
          if (!session.isExpired) sessions += parts(0) -> session
        })
  }

  def addSession(username: String, session: Session) = sessions += username -> session
  def findSession(username: String): Option[Session] = Option(sessions.getOrElse(username, null))

  def persist() {
    val sb = new StringBuilder
    sessions.foreach((item) => sb ++= s"${item._1}/${item._2.modhash}/${item._2.cookie}/${item._2.expiration.getTime}\n")
    val pw = new java.io.PrintWriter(sessionsFile)
    pw.println(sb.mkString.dropRight(1))
    pw.flush
    pw.close
  }

}