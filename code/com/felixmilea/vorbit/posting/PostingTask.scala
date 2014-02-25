package com.felixmilea.vorbit.posting

import akka.actor.Props
import akka.routing.BalancingPool
import com.felixmilea.vorbit.reddit.mining.config.PostSort
import com.felixmilea.vorbit.actors.PostingCoordinator
import com.felixmilea.vorbit.actors.RedditDownloader.DownloadRequest
import com.felixmilea.vorbit.actors.RedditDownloader.Listing
import com.felixmilea.vorbit.utils.Loggable
import com.felixmilea.vorbit.utils.JSON
import com.felixmilea.vorbit.utils.AppUtils

class PostingTask(posterName: String, config: JSON) extends Thread with Loggable {
  private[this] val coordinator = AppUtils.actorSystem.actorOf(Props(new PostingCoordinator(config)).withRouter(BalancingPool(20)), config.name)
  private[this] val coordinatorSel = AppUtils.actor(coordinator.path)
  private[this] val downloader = AppUtils.actor(AppUtils.actorSystem.child(PostingManager.Names.download))

  override def run() {
    do {
      Info(s"Starting reddit poster '$posterName/${config.name}'")
      for (targetId <- 0 until config.task.targets.length) {
        val target = config.task.targets(targetId)
        for (unit <- target.units) {
          val postSort = PostSort.withName(config.task.postSort)
          downloader ! DownloadRequest(Listing(unit, postSort, config.task.postLimit, config.task.time, config.task.postListings), coordinatorSel, targetId.toString)
        }
      }

      if (config.task.recurrence != 0) {
        Debug(s"   -- Posting task ${config.dataset}/${config.task.name} queued and will requeue in ${config.task.recurrence}ms")
        Thread.sleep(config.recurrence.toInt)
      } else {
        Debug(s"   -- Posting task ${config.dataset}/${config.task.name} queued")
      }
    } while (config.recurrence != 0)
  }

}