package com.felixmilea.vorbit.posting

import akka.routing.BalancingPool
import akka.actor.Props
import com.felixmilea.vorbit.actors.ActorManager
import com.felixmilea.vorbit.actors.Poster
import com.felixmilea.vorbit.actors.RedditDownloader

class PostingManager extends ActorManager {

  override protected[this] lazy val children = {
    List("Posters" -> context.actorOf(BalancingPool(RedditUserManager.users.size).props(Props[Poster]), "Posters"))
      .::("Downloaders" -> context.actorOf(BalancingPool(10).props(Props[RedditDownloader])))
  }.toMap

}