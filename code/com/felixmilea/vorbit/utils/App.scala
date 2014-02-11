package com.felixmilea.vorbit.utils

import akka.actor.ActorSystem
import akka.actor.Props
import akka.actor.ActorRef

object App {
  lazy val actorSystem: ActorSystem = ActorSystem("Application")
  lazy val log: ActorRef = actorSystem.actorOf(Props[Log], "Log")
  lazy val config: ConfigManager = new ConfigManager("config/")
  def actor(name: String) = actorSystem.actorSelection(s"akka://Application/user/$name")
}