package com.felixmilea.vorbit.reddit.mining

import scala.concurrent.duration._
import scala.concurrent.Await
import akka.actor.Props
import akka.actor.ActorRef
import akka.routing.SmallestMailboxRouter
import akka.pattern.ask
import akka.util.Timeout
import com.felixmilea.vorbit.utils.AppUtils
import com.felixmilea.vorbit.utils.MappedProps
import com.felixmilea.vorbit.reddit.mining.actors.ActorManager
import com.felixmilea.vorbit.reddit.mining.actors.ActorManager._
import com.felixmilea.vorbit.reddit.mining.actors.RedditDownloader
import com.felixmilea.vorbit.reddit.mining.actors.TaskRecorder
import com.felixmilea.vorbit.reddit.mining.actors.PostValidator
import com.felixmilea.vorbit.reddit.mining.actors.PostProcessor
import com.felixmilea.vorbit.reddit.mining.actors.TextUnitProcessor
import com.felixmilea.vorbit.reddit.mining.actors.NgramProcessor

class MiningManager(minerCount: Int = 1) {
  import MiningManager._

  private val managedActors = Map(
    (ActorNames.downloader -> Props[RedditDownloader].withRouter(SmallestMailboxRouter(15 * minerCount))),
    (ActorNames.taskRecorder -> Props[TaskRecorder].withRouter(SmallestMailboxRouter(1 * minerCount))),
    (ActorNames.validator -> Props[PostValidator].withRouter(SmallestMailboxRouter(5 * minerCount))),
    (ActorNames.postProcessor -> Props[PostProcessor].withRouter(SmallestMailboxRouter(10 * minerCount))),
    (ActorNames.textProcessor -> Props[TextUnitProcessor].withRouter(SmallestMailboxRouter(15 * minerCount))),
    (ActorNames.ngramProcessor -> Props[NgramProcessor].withRouter(SmallestMailboxRouter(15 * minerCount))) // last
    )

  private[this] val actorManager = AppUtils.actorSystem.actorOf(Props(new ActorManager(managedActors)), "MiningActorManager")

  def ping() = actorManager ! PingChildren()

  implicit val timeout = Timeout(3 seconds)
  val future = actorManager ? GetChildren()

  val actors = new MappedProps[ActorRef] {
    val propMap = Await.result(future, timeout.duration).asInstanceOf[Map[String, ActorRef]]
  }
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