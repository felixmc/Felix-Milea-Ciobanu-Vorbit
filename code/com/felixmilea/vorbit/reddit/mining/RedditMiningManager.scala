package com.felixmilea.vorbit.reddit.mining

import akka.actor.Props
import akka.routing.SmallestMailboxRouter
import com.felixmilea.vorbit.reddit.mining.actors.ActorManager
import com.felixmilea.vorbit.reddit.mining.actors.ActorManager._
import com.felixmilea.vorbit.reddit.mining.actors.RedditDownloader
import com.felixmilea.vorbit.reddit.mining.actors.TaskRecorder
import com.felixmilea.vorbit.reddit.mining.actors.PostValidator
import com.felixmilea.vorbit.reddit.mining.actors.PostProcessor
import com.felixmilea.vorbit.reddit.mining.actors.TextUnitProcessor
import com.felixmilea.vorbit.reddit.mining.actors.NgramProcessor

class RedditMiningManager(minerCount: Int = 1) extends MiningManager(minerCount) {
  import MiningManager._

  val name = "RedditMiningManager"
  val managedActors = Map(
    (ActorNames.downloader -> Props[RedditDownloader].withRouter(SmallestMailboxRouter(15 * minerCount))),
    (ActorNames.taskRecorder -> Props[TaskRecorder]),
    (ActorNames.validator -> Props[PostValidator].withRouter(SmallestMailboxRouter(5 * minerCount))),
    (ActorNames.postProcessor -> Props[PostProcessor].withRouter(SmallestMailboxRouter(10 * minerCount))),
    (ActorNames.textProcessor -> Props[TextUnitProcessor].withRouter(SmallestMailboxRouter(15 * minerCount))),
    (ActorNames.ngramProcessor -> Props[NgramProcessor].withRouter(SmallestMailboxRouter(15 * minerCount))) // last
    )

  init()

}