package com.felixmilea.vorbit.posting

import akka.actor.Props
import akka.routing.BalancingPool
import com.felixmilea.vorbit.actors.Composer
import com.felixmilea.vorbit.composition.NgramManager
import com.felixmilea.vorbit.utils.JSON
import com.felixmilea.vorbit.utils.Loggable
import com.felixmilea.vorbit.utils.AppUtils

class RedditPoster(config: JSON) extends Thread with Loggable {
  Info("Starting RedditPoster '" + config.name + "'")

  private[this] val dataset = AppUtils.config.persistence.data.datasets(config.corpus.dataset)
  private[this] val subset = AppUtils.config.persistence.data.subsets(config.corpus.subset)
  private[this] val edition = AppUtils.config.persistence.data.editions(config.corpus.edition)

  override def run() {
    val ngrams = new NgramManager(config.corpus.n, dataset, subset, edition)
    val composerActor = AppUtils.actorSystem.actorOf(Props(new Composer(ngrams)).withRouter(BalancingPool(10)), config.name + "-" + PostingManager.Names.composer)
    Debug("   -- Initializing actor " + composerActor.path)

    for (taskId <- 0 until config.tasks.length) {
      val postingTask = new PostingTask(config, taskId)
      postingTask.start()
    }
  }

}