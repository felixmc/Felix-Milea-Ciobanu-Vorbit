package com.felixmilea.vorbit.reddit.mining

import akka.routing.BalancingPool
import akka.actor.ActorRef
import akka.actor.Props
import com.felixmilea.vorbit.reddit.mining.config.ConfigState
import com.felixmilea.vorbit.reddit.mining.config.TaskConfig
import com.felixmilea.vorbit.reddit.mining.config.TargetType
import com.felixmilea.vorbit.actors.RedditDownloader._
import com.felixmilea.vorbit.actors.MiningCoordinator
import com.felixmilea.vorbit.actors.TaskRecorder.UpdateTask
import com.felixmilea.vorbit.utils.Loggable
import com.felixmilea.vorbit.utils.AppUtils

class MiningTask(config: TaskConfig, manager: ActorRef) extends Thread with Loggable {
  private[this] val coordinator = AppUtils.actorSystem.actorOf(Props(new MiningCoordinator(config)).withRouter(BalancingPool(20)), config.dataset + "-" + config.task.name)
  private[this] val coordinatorSel = AppUtils.actor(coordinator.path)
  private[this] val taskRecorder = AppUtils.actor(AppUtils.actorSystem.child(RedditMiningManager.Names.task))
  private[this] val downloader = AppUtils.actor(AppUtils.actorSystem.child(RedditMiningManager.Names.download))

  Debug(s"   -- Initializing MiningCoordinator ${coordinator.path}")

  override def run() {
    do {
      Info(s"Starting mining task '${config.dataset}/${config.task.name}'")
      for (targetId <- 0 until config.task.targets.length) {
        val target = config.task.targets(targetId)
        //        val conf = ConfigState(config.dataset, config.task, target)
        for (unit <- target.units) {
          if (config.task.targetType == TargetType.Subreddit)
            downloader ! DownloadRequest(Listing(unit, config.task.postSort, config.task.postLimit, config.task.time, config.task.postListings), coordinatorSel, targetId.toString)
          else if (config.task.targetType == TargetType.User)
            downloader ! DownloadRequest(UserComments(unit, config.task.commentSort, config.task.postLimit, config.task.postListings), coordinatorSel, targetId.toString)
        }
      }

      taskRecorder ! UpdateTask(config.dataset, config.task.name)

      if (config.task.recurrence != 0) {
        Debug(s"   -- Mining task ${config.dataset}/${config.task.name} queued and will requeue in ${config.task.recurrence}ms")
        Thread.sleep(config.task.recurrence)
      } else {
        Debug(s"   -- Mining task ${config.dataset}/${config.task.name} queued")
      }
    } while (config.task.recurrence != 0)
  }

}