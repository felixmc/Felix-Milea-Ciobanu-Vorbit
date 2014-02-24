package com.felixmilea.vorbit.reddit.mining

import akka.actor.Props
import com.felixmilea.vorbit.reddit.mining.config.MinerConfig
import com.felixmilea.vorbit.actors.RedditCorpusRetriever._
import com.felixmilea.vorbit.utils.AppUtils
import com.felixmilea.vorbit.utils.Loggable
import com.felixmilea.vorbit.actors.ActorManager.PingChildren

class SubsetMiner(dataset: Int, subsets: Tuple2[Int, Int], edition: Int) extends Thread with Loggable {
  val manager = AppUtils.actorSystem.actorOf(Props(new SubsetAnalysisManager(dataset, subsets, edition)), "SubsetMiner")

  private[this] val downloader = AppUtils.actor(manager.path.child(SubsetAnalysisManager.ActorNames.downloader))
  private[this] val coordinator = AppUtils.actor(manager.path.child(SubsetAnalysisManager.ActorNames.coordinator))

  def ping() = manager ! PingChildren()

  override def run() {
    Info(s"Started subset comparison analysis on subsets $subsets of dataset $dataset")
    downloader ! Request(Posts(dataset, subsets._1), coordinator)
  }

}