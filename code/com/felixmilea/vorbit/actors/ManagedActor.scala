package com.felixmilea.vorbit.actors

import scala.util.Random
import akka.actor.Actor
import com.felixmilea.vorbit.utils.Loggable
import com.felixmilea.vorbit.utils.AppUtils
import com.felixmilea.vorbit.reddit.mining.config.ConfigState
import com.felixmilea.vorbit.utils.JSON

abstract class ManagedActor(protected[this] val inPool: Boolean = true) extends Actor with Loggable {
  import ActorManager._

  val qualifiedName = self.path.toString().replaceFirst(AppUtils.baseActorPath, "")
  override protected[this] def wrapLog(message: String): String = s"${qualifiedName}: $message"

  protected[this] val selfPool = if (inPool) context.parent else self

  protected[this] def sibling(name: String): Any = {
    val parent = if (inPool) self.path.parent.parent else self.path.parent
  }

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
}