package com.felixmilea.vorbit.actors

import java.util.Date
import akka.actor.Props
import akka.actor.Actor
import akka.actor.ActorRef
import com.felixmilea.vorbit.utils.Loggable
import com.felixmilea.vorbit.utils.AppUtils

abstract class ActorManager extends Actor with Loggable {
  import ActorManager._

  protected[this] val name: String
  protected[this] val actors: Map[String, Props]
  protected[this] lazy val children: Map[String, ActorRef] = {
    actors.map(a => {
      val actor = AppUtils.actorSystem.actorOf(a._2, a._1)
      Debug(s"   -- Initializing actor ${actor.path}")
      (a._1, actor)
    })
  }

  override def preStart() {
    Info(s"Starting $name")
    children
  }

  private[this] var lastPong: Pong = null
  private[this] var pongs = -1

  def receive = {
    case PingChildren => {
      Info("got ping children")
      if (pongs == -1) {
        children.foreach(_._2 ! Ping())
        pongs = 0
      }
    }

    case PingStatus => {
      // awaiting ping
      if (pongs != -1) {
        sender ! NotDone
      } // if not awaiting ping, but have in the past
      else if (lastPong != null) {
        sender ! Done(lastPong.received, lastPong.elapsed)
      }
    }

    case p: Pong =>
      if (pongs != -1) {
        pongs = 1 + pongs
        if (pongs == children.size) {
          Info(s"${self.path}: children took ${p.elapsed}ms to respond.")
          lastPong = p
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

  trait DoneStatus
  trait ManagerCommand

  case class Done(finished: Date, duration: Long) extends DoneStatus
  case class NotDone extends DoneStatus

  case class PingChildren extends ManagerCommand
  case class PingStatus extends ManagerCommand
}