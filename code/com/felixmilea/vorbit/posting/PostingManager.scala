package com.felixmilea.vorbit.posting

import akka.routing.BalancingPool
import akka.actor.Props
import com.felixmilea.vorbit.actors.ActorManager
import com.felixmilea.vorbit.actors.Poster
import com.felixmilea.vorbit.actors.RedditDownloader

class PostingManager(posters: Int) extends ActorManager {
  import PostingManager._

  protected[this] val name: String = "PostingManager"

  override protected[this] lazy val actors = {
    List(Names.poster -> Props[Poster].withRouter(BalancingPool(RedditUserManager.users.size)))
      .::(Names.download -> Props[RedditDownloader].withRouter(BalancingPool(posters * 10)))
  }.toMap

}

object PostingManager {
  object Names {
    val download = "Downloader"
    val validator = "Validator"
    val composer = "Composer"
    val poster = "Poster"
    val recorder = "PostRecorder"
  }
}