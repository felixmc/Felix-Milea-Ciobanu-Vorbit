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
import scala.util.Random

class PostingTask(config: JSON, taskId: Int) extends Thread with Loggable {
  private[this] val task = config.tasks(taskId)
  private[this] val seed = Random.nextInt(1000000)
  private[this] val coordinator = AppUtils.actorSystem.actorOf(Props(new PostingCoordinator(config, task)).withRouter(BalancingPool(20)), config.name + "-" + task.name + "_" + seed)
  private[this] val coordinatorSel = AppUtils.actor(coordinator.path)
  private[this] val downloader = AppUtils.actor(AppUtils.actorSystem.child(PostingManager.Names.download))

  private[this] var stopFlag = false

  // 0 => stopped | 1 => posting | 2 => sleeping | 3 => done
  private[this] var status = 0

  override def run() {
    do {
      Info(s"Starting posting task '${config.name}/${task.name}'")
      status = 1
      for (targetId <- 0 until task.targets.length) {
        val target = task.targets(targetId)
        for (sub <- target.subreddits) {
          val postSort = PostSort.withName(task.postSort)
          downloader ! DownloadRequest(Listing(sub, postSort, task.postLimit, task.time, task.postListings), coordinatorSel, targetId.toString)
        }
      }

      if (task.recurrence != 0) {
        Debug(s"   -- Posting task ${config.name}/${task.name} queued and will requeue in ${task.recurrence.toInt}ms")
        status = 2
        Thread.sleep(task.recurrence.toInt)
      } else {
        Debug(s"   -- Posting task ${config.name}/${task.name} queued")
      }
    } while (config.recurrence.toInt != 0)

    status = 3
  }

  def getStatus(): Int = status

  def gentleStop() {
    stopFlag = true
    while (status == 1) { Thread.sleep(500) }
    this.stop()
  }

  def forceStop() {
    AppUtils.actorSystem.stop(coordinator)
    this.stop()
  }

}