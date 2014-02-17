package com.felixmilea.vorbit.reddit.mining.actors

import scala.util.Random
import akka.actor.Actor
import com.felixmilea.vorbit.utils.Loggable
import com.felixmilea.vorbit.utils.AppUtils
import com.felixmilea.vorbit.reddit.mining.config.ConfigState

trait ManagedActor extends Actor with Loggable {
  import ActorManager._

  val qualifiedName = self.path.toString().replaceFirst(AppUtils.baseActorPath, "")
  override protected[this] def wrapLog(message: String): String = s"${qualifiedName}: $message"

  final def receive = {
    case p: Ping => {
      val pong = Pong(p)
      Debug("ping: " + pong.elapsed + "ms")
      sender ! pong
    }
    case ManagedActor.Sleep(time) => Thread.sleep(time)
    case default => doReceive(default)
  }

  def doReceive: Actor.Receive
}

object ManagedActor {
  case class Sleep(amount: Int = Random.nextInt(1000) + 200)
  trait WorkCommand
  case class MinerCommand(work: WorkCommand, conf: ConfigState)
}