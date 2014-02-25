package com.felixmilea.vorbit.posting

import akka.routing.BalancingPool
import akka.actor.Props
import com.felixmilea.vorbit.actors.ActorManager
import com.felixmilea.vorbit.actors.Poster
import com.felixmilea.vorbit.actors.RedditDownloader

class PostingManager extends ActorManager {

  protected[this] val name: String = "PostingManager"

  override protected[this] lazy val actors = {
    List("Posters" -> Props[Poster].withRouter(BalancingPool(RedditUserManager.users.size)))
      .::("Downloaders" -> Props[RedditDownloader].withRouter(BalancingPool(10)))
  }.toMap

}