package com.felixmilea.vorbit.utils

import akka.actor.ActorSystem
import akka.actor.Props
import akka.actor.ActorRef

object ApplicationUtils {
  private var actorSystem: ActorSystem = null
  private var log: ActorRef = null

  def getActorSystem: ActorSystem = {
    if (actorSystem == null) {
      actorSystem = ActorSystem("Application")
    }
    return actorSystem
  }

  def getLog: ActorRef = {
    if (log == null) {
      log = getActorSystem.actorOf(Props[Log], "Log")
    }
    return log
  }
}