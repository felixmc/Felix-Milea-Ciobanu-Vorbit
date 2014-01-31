package com.felixmilea.vorbit.utils

import akka.actor.ActorSystem
import akka.actor.Props
import akka.actor.ActorRef

object ApplicationUtils {
  lazy val actorSystem: ActorSystem = ActorSystem("Application")
  lazy val log: ActorRef = actorSystem.actorOf(Props[Log], "Log")
}