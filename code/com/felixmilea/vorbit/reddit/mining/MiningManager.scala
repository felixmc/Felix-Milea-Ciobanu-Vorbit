package com.felixmilea.vorbit.reddit.mining

import scala.concurrent.duration._
import scala.concurrent.Await
import akka.actor.Props
import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout
import com.felixmilea.vorbit.reddit.mining.actors.ActorManager
import com.felixmilea.vorbit.reddit.mining.actors.ActorManager._
import com.felixmilea.vorbit.utils.AppUtils
import com.felixmilea.vorbit.utils.MappedProps

abstract class MiningManager(protected val minerCount: Int = 1) {
  import MiningManager._

  protected[this] val managedActors: Map[String, Props]
  protected[this] val name: String
  protected[this] lazy val actorManager = AppUtils.actorSystem.actorOf(Props(new ActorManager(managedActors)), name)

  final lazy val actors = {
    implicit val timeout = Timeout(3 seconds)
    val future = actorManager ? GetChildren()
    new MappedProps[ActorRef] {
      val propMap = Await.result(future, timeout.duration).asInstanceOf[Map[String, ActorRef]]
    }
  }

  final protected[this] def init() {
    actorManager
    actors
  }

  final def ping() = actorManager ! PingChildren()
}

object MiningManager {
  object ActorNames {
    val downloader = "downloader"
    val taskRecorder = "taskRecorder"
    val validator = "validator"
    val postProcessor = "postProcessor"
    val textProcessor = "textProcessor"
    val ngramProcessor = "ngramProcessor"
  }
}