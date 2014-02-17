package com.felixmilea.vorbit.reddit.mining

import com.felixmilea.vorbit.utils.Loggable
import com.felixmilea.vorbit.reddit.mining.config.TaskConfig
import com.felixmilea.vorbit.reddit.mining.config.ConfigState
import com.felixmilea.vorbit.reddit.mining.actors.RedditDownloader._
import com.felixmilea.vorbit.reddit.mining.actors.ManagedActor.MinerCommand
import com.felixmilea.vorbit.reddit.mining.actors.TaskRecorder.UpdateTask
import com.felixmilea.vorbit.utils.AppUtils

class MiningTask(config: TaskConfig, manager: MiningManager) extends Thread with Loggable {

  override def run() {
    do {
      Info(s"Launching mining task ${config.dataset}/${config.task.name}")
      for (target <- config.task.targets) {
        val conf = ConfigState(config.dataset, config.task, target)
        for (unit <- target.units) {
          manager.actors.downloader ! MinerCommand(DownloadRequest(Listing(unit, config.task.postListings), AppUtils.actor(manager.actors.validator.path)), conf)
        }
      }

      manager.actors.taskRecorder ! UpdateTask(config.dataset, config.task.name)

      if (config.task.recurrence != 0) {
        Info(s"Sleeping mining task ${config.dataset}/${config.task.name} for ${config.task.recurrence}ms")
        Thread.sleep(config.task.recurrence)
      }
    } while (config.task.recurrence != 0)
  }

}