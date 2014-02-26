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

class PostingTask(config: JSON, taskId: Int) extends Thread with Loggable {
  private[this] val task = config.tasks(taskId)

  private[this] val coordinator = AppUtils.actorSystem.actorOf(Props(new PostingCoordinator(config, task)).withRouter(BalancingPool(20)), config.name + "-" + task.name)
  private[this] val coordinatorSel = AppUtils.actor(coordinator.path)
  private[this] val downloader = AppUtils.actor(AppUtils.actorSystem.child(PostingManager.Names.download))

  override def run() {
    do {
      Info(s"Starting posting task '${config.name}/${task.name}'")
      for (targetId <- 0 until task.targets.length) {
        val target = task.targets(targetId)
        for (sub <- target.subreddits) {
          val postSort = PostSort.withName(task.postSort)
          downloader ! DownloadRequest(Listing(sub, postSort, task.postLimit, task.time, task.postListings), coordinatorSel, targetId.toString)
        }
      }

      if (task.recurrence != 0) {
        Debug(s"   -- Posting task ${config.name}/${task.name} queued and will requeue in ${task.recurrence.toInt}ms")
        Thread.sleep(task.recurrence.toInt)
      } else {
        Debug(s"   -- Posting task ${config.name}/${task.name} queued")
      }
    } while (config.recurrence != 0)
  }

}