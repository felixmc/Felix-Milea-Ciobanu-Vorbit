package com.felixmilea.vorbit.reddit.mining

import akka.actor.Props
import akka.routing.BalancingPool
import akka.routing.SmallestMailboxRouter
import com.felixmilea.vorbit.actors.ActorManager
import com.felixmilea.vorbit.actors.RedditDownloader
import com.felixmilea.vorbit.actors.TaskRecorder
import com.felixmilea.vorbit.actors.PostProcessor
import com.felixmilea.vorbit.actors.TextUnitProcessor
import com.felixmilea.vorbit.actors.NgramProcessor
import com.felixmilea.vorbit.actors.RedditPostValidator

class RedditMiningManager(minerCount: Int = 1) extends ActorManager {
  import RedditMiningManager._

  override protected[this] lazy val actors = {
    List(Names.download -> BalancingPool(15 * minerCount).props(Props[RedditDownloader]))
      .::(Names.task -> Props[TaskRecorder])
      .::(Names.validator -> Props[RedditPostValidator].withRouter(BalancingPool(5 * minerCount)))
      .::(Names.post -> Props[PostProcessor].withRouter(BalancingPool(10 * minerCount)))
      .::(Names.text -> Props[TextUnitProcessor].withRouter(BalancingPool(15 * minerCount)))
      .::(Names.ngram -> Props[NgramProcessor].withRouter(BalancingPool(15 * minerCount)))
  }.toMap

}

object RedditMiningManager {
  object Names {
    val download = "Downloader"
    val task = "TaskRecorder"
    val validator = "Validator"
    val post = "PostProcessor"
    val text = "TextUnitProcessor"
    val ngram = "NgramProcessor"
  }
}