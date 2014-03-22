package com.felixmilea.vorbit.posting

import akka.routing.BalancingPool
import akka.actor.Props
import com.felixmilea.vorbit.actors.ActorManager
import com.felixmilea.vorbit.actors.Poster
import com.felixmilea.vorbit.actors.RedditDownloader
import com.felixmilea.vorbit.actors.RedditPostValidator
import com.felixmilea.vorbit.actors.PostRecorder

class PostingManager(posters: Int) extends ActorManager {
  import PostingManager._

  def this() = this(1)

  protected[this] val name: String = "PostingManager"

  override protected[this] lazy val actors = {
    List(Names.download -> Props[RedditDownloader].withRouter(BalancingPool(posters * 10)))
      .::(Names.validator -> Props[RedditPostValidator].withRouter(BalancingPool(posters * 10)))
      .::(Names.recorder -> Props[PostRecorder].withRouter(BalancingPool(posters * 20)))
      .::(Names.poster -> Props(new Poster(RedditUserManager.grabNext)).withRouter(BalancingPool(RedditUserManager.usersMap.size)))
  }.toMap

}

object PostingManager {
  object Names {
    val download = "PDownloader"
    val validator = "PValidator"
    val recorder = "PostRecorder"
    val composer = "Composer"
    val poster = "Poster"
  }
}