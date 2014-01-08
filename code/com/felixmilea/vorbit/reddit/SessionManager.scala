package com.felixmilea.vorbit.reddit

import sys.process._
import scala.io.Source
import java.util.Date

object SessionManager {
  private val sessionsFile = "appdata/sessions.data"
  private var sessions: scala.collection.mutable.Map[String, Session] = null

  def init() {
    sessions = new scala.collection.mutable.HashMap[String, Session]
    if (new java.io.File(sessionsFile).exists)
      Source.fromFile(sessionsFile).getLines.foreach(line =>
        if (!line.isEmpty()) {
          val parts = line.split("/")
          val session = new Session(parts(1), parts(2), new Date(parts(3).toLong))
          if (!session.isExpired) sessions += parts(0) -> session
        })
  }

  def addSession(username: String, session: Session) = sessions += username -> session
  def findSession(username: String): Option[Session] = Option(sessions.getOrElse(username, null))

  def persist() {
    val sb = new StringBuilder
    sessions.foreach((item) => sb ++= s"${item._1}/${item._2.modhash}/${item._2.cookie}/${item._2.expiration.getTime}\n")
    s"echo ${sb.mkString.dropRight(1)}" #> new java.io.File(sessionsFile) !
  }

}