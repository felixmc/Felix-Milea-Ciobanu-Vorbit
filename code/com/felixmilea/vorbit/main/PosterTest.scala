package com.felixmilea.vorbit.main

import akka.actor.Props
import com.felixmilea.vorbit.actors.ActorManager.PingChildren
import com.felixmilea.vorbit.posting.PostingManager
import com.felixmilea.vorbit.posting.RedditPoster
import com.felixmilea.vorbit.utils.AppUtils
import com.felixmilea.vorbit.utils.Loggable

object PosterTest extends App with Loggable {
  val manager = AppUtils.actorSystem.actorOf(Props(new PostingManager(AppUtils.config("posters").length)))

  //  start posters based on config
  for (config <- AppUtils.config("posters")) {
    try {
      val poster = new RedditPoster(config)
      poster.start()
    } catch {
      case t: Throwable => {
        Error(s"Unexpected error occured while creating or running poster #${config.name}: ${t.getMessage}")
      }
    }
  }

  while (true) {
    readLine()
    manager ! PingChildren()
  }

}