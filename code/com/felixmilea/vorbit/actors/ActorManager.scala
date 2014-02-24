package com.felixmilea.vorbit.actors

import java.util.Date
import akka.actor.Props
import akka.actor.Actor
import akka.actor.ActorRef
import com.felixmilea.vorbit.utils.Loggable

class ActorManager(actors: Map[String, Props] = Map()) extends Actor with Loggable {
  import ActorManager._

  protected[this] lazy val children: Map[String, ActorRef] = actors.map(a => (a._1, context.actorOf(a._2, a._1)))

  override def preStart() { children }

  private[this] var pongs = -1

  def receive = {
    case GetChildren() => sender ! children
    case PingChildren() =>
      if (pongs == -1) {
        children.foreach(_._2 ! Ping())
        pongs = 0
      }
    case p: Pong =>
      if (pongs != -1) {
        pongs = 1 + pongs
        if (pongs == children.size) {
          Info(s"${self.path}: children took ${p.elapsed}ms to respond.")
          pongs = -1
        }
      }
  }
}

object ActorManager {
  case class Ping(sent: Date = new Date())
  case class Pong(ping: Ping, received: Date = new Date()) {
    val elapsed = received.getTime - ping.sent.getTime
  }

  case class PingChildren()
  case class GetChildren()
}