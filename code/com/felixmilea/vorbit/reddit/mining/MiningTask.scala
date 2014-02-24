package com.felixmilea.vorbit.reddit.mining

import akka.routing.BalancingPool
import akka.actor.ActorRef
import akka.actor.Props
import com.felixmilea.vorbit.reddit.mining.config.TaskConfig
import com.felixmilea.vorbit.reddit.mining.config.ConfigState
import com.felixmilea.vorbit.actors.RedditDownloader._
import com.felixmilea.vorbit.actors.MiningCoordinator
import com.felixmilea.vorbit.actors.TaskRecorder.UpdateTask
import com.felixmilea.vorbit.utils.Loggable
import com.felixmilea.vorbit.utils.AppUtils

class MiningTask(config: TaskConfig, manager: ActorRef) extends Thread with Loggable {

  private[this] val coordinator = AppUtils.actorSystem.actorOf(Props(new MiningCoordinator(manager, config)).withRouter(BalancingPool(10)))
  private[this] val coordinatorSel = AppUtils.actor(coordinator.path)
  private[this] val taskRecorder = AppUtils.actor(manager.path.child(RedditMiningManager.Names.task))
  private[this] val downloader = AppUtils.actor(manager.path.child(RedditMiningManager.Names.download))

  override def run() {
    do {
      Info(s"Launching mining task ${config.dataset}/${config.task.name}")
      for (targetId <- 0 until config.task.targets.length) {
        val target = config.task.targets(targetId)
        //        val conf = ConfigState(config.dataset, config.task, target)
        for (unit <- target.units) {
          downloader ! DownloadRequest(Listing(unit, config.task.postSort, config.task.postLimit, config.task.time, config.task.postListings), coordinatorSel, targetId.toString)
        }
      }

      taskRecorder ! UpdateTask(config.dataset, config.task.name)

      if (config.task.recurrence != 0) {
        Info(s"Sleeping mining task ${config.dataset}/${config.task.name} for ${config.task.recurrence}ms")
        Thread.sleep(config.task.recurrence)
      }
    } while (config.task.recurrence != 0)
  }

}