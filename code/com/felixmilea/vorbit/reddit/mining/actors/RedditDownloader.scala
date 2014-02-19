package com.felixmilea.vorbit.reddit.mining.actors

import akka.actor.Actor
import akka.actor.ActorSelection
import com.felixmilea.vorbit.utils.JSON
import com.felixmilea.vorbit.reddit.mining.actors.ManagedActor.WorkCommand
import com.felixmilea.vorbit.reddit.mining.actors.ManagedActor.MinerCommand
import com.felixmilea.vorbit.reddit.mining.config.PostSort
import com.felixmilea.vorbit.reddit.mining.config.ConfigState
import com.felixmilea.vorbit.reddit.connectivity.Client
import com.felixmilea.vorbit.utils.JSONException
import com.felixmilea.vorbit.utils.AppUtils
import com.felixmilea.vorbit.reddit.mining.MiningManager

class RedditDownloader extends ManagedActor {
  import RedditDownloader._

  private[this] lazy val downloader = AppUtils.actor(self.path.parent.parent.child(MiningManager.ActorNames.downloader))
  private[this] lazy val client = new Client

  def doReceive = {
    case MinerCommand(work, conf) => work match {
      case DownloadRequest(item, receiver) => item match {
        case Listing(sub, pages, after) => {
          val url = subredditUrl(sub, conf.task.postSort, conf.task.postLimit, conf.task.time, after)
          try {
            val listing = client.getJSON(url)("data")("children")
            receiver ! ListingResult(listing, conf)
            if (pages > 0 && listing.length > 0) {
              downloader ! MinerCommand(DownloadRequest(Listing(sub, pages - 1, listing(listing.length - 1)("data")("id")), receiver), conf)
            }
          } catch {
            case e: JSONException => {
              Error(s"Error while parsing or traversing JSON retrieved from '$url'")
            }
          }
        }
        case Post(id) => {
          val url = s"comments/$id.json?sort=${conf.task.commentSort}"
          try {
            val data = client.getJSON(url)
            receiver ! PostResult(data, conf)
          } catch {
            case e: JSONException => {
              Error(s"Error while parsing or traversing JSON retrieved from '$url'")
            }
          }
        }
      }
    }
  }

  def subredditUrl(sub: String, sort: PostSort.PostSort = PostSort.Hot, limit: Int = 25, time: String = "", after: String = ""): String = {
    val vars = (if (!time.isEmpty) s"&t=${time}" else "") + (if (after.isEmpty()) "" else s"&after=t3_$after")
    return s"r/$sub/$sort/.json?limit=$limit$vars"
  }
}

object RedditDownloader {
  trait DownloadResult
  case class ListingResult(json: JSON, conf: ConfigState) extends DownloadResult
  case class PostResult(json: JSON, conf: ConfigState) extends DownloadResult

  trait Downloadable
  case class Listing(subreddit: String, pages: Int, after: String = "") extends Downloadable
  case class Post(redditId: String) extends Downloadable

  case class DownloadRequest(item: Downloadable, receiver: ActorSelection) extends WorkCommand
}