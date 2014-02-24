package com.felixmilea.vorbit.reddit.mining

import akka.actor.Props
import akka.routing.SmallestMailboxRouter
import com.felixmilea.vorbit.actors.ActorManager
import com.felixmilea.vorbit.actors.ActorManager._
import com.felixmilea.vorbit.actors.RedditDownloader
import com.felixmilea.vorbit.actors.TaskRecorder
import com.felixmilea.vorbit.actors.PostValidator
import com.felixmilea.vorbit.actors.PostProcessor
import com.felixmilea.vorbit.actors.TextUnitProcessor
import com.felixmilea.vorbit.actors.NgramProcessor
import com.felixmilea.vorbit.actors.ActorSetManager

class RedditMiningManager(minerCount: Int = 1) extends ActorSetManager(minerCount) {
  import ActorSetManager._

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