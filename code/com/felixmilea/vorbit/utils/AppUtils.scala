package com.felixmilea.vorbit.utils

import akka.actor.ActorSystem
import akka.actor.Props
import akka.actor.ActorRef
import akka.actor.ActorPath

object AppUtils {
  lazy val actorSystem: ActorSystem = ActorSystem("Application")
  lazy val log: ActorRef = actorSystem.actorOf(Props[Log], "Log")
  lazy val config: ConfigManager = new ConfigManager("config/")
  val baseActorPath: String = "akka://Application/user/"
  def actor(path: String) = actorSystem.actorSelection(s"$baseActorPath$path")
  def actor(path: ActorPath) = actorSystem.actorSelection(path)
}