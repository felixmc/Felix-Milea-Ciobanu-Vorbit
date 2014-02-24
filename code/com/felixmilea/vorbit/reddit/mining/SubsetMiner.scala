package com.felixmilea.vorbit.reddit.mining

import com.felixmilea.vorbit.reddit.mining.config.MinerConfig
import com.felixmilea.vorbit.actors.RedditCorpusRetriever._
import com.felixmilea.vorbit.utils.AppUtils

class SubsetMiner(dataset: Int, subsets: Tuple2[Int, Int], edition: Int) extends Miner {
  val manager = new SubsetAnalysisManager()

  //  private[this] val dataset = AppUtils.config.persistence.data.datasets(config.dataset)

  def ping() = manager.ping

  override def run() {
    Info(s"Started subset comparison analysis on subsets $subsets of dataset $dataset")
    manager.actors.downloader ! Request(Posts(dataset, subsets._1), AppUtils.actor(manager.actors.coordinator.path))
  }

}