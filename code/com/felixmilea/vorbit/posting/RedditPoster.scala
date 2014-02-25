package com.felixmilea.vorbit.posting

import com.felixmilea.vorbit.utils.JSON
import com.felixmilea.vorbit.utils.Loggable

class RedditPoster(config: JSON) extends Thread with Loggable {

  Info("Starting RedditPoster '" + config.name + "'")

  override def run() {
    for (task <- config.tasks) {
      val postingTask = new PostingTask(config.name.toString, task)
      postingTask.start()
    }
  }

}