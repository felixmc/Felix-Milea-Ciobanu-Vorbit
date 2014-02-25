package com.felixmilea.vorbit.actors

import akka.actor.ActorSelection
import com.felixmilea.vorbit.utils.JSON
import com.felixmilea.vorbit.actors.ManagedActor.WorkCommand
import com.felixmilea.vorbit.reddit.mining.config.PostSort
import com.felixmilea.vorbit.reddit.mining.config.ConfigState
import com.felixmilea.vorbit.reddit.connectivity.Client
import com.felixmilea.vorbit.utils.JSONException
import com.felixmilea.vorbit.utils.AppUtils
import com.felixmilea.vorbit.reddit.mining.config.CommentSort
import java.io.IOException

class RedditDownloader extends ManagedActor {
  import RedditDownloader._

  private[this] lazy val client = new Client

  def doReceive = {
    case DownloadRequest(item, receiver, tag) => item match {
      case Listing(sub, sort, count, time, pages, after) => {
        val url = subredditUrl(sub, sort, count, time, after)
        try {
          val listing = client.getJSON(url)("data")("children")
          receiver ! PostListingResult(listing, tag)
          if (pages > 0 && listing.length > 0) {
            selfPool ! DownloadRequest(Listing(sub, sort, count, time, pages - 1, listing(listing.length - 1)("data")("id")), receiver, tag)
          }
        } catch {
          case jsone: JSONException => {
            Error(s"Error while parsing or traversing JSON retrieved from '$url': " + jsone.getMessage)
          }
          case ioe: IOException => {
            Error(s"Connection error while downloading listing '$url' (requeuing): " + ioe.getMessage)
            Thread.sleep(1000)
            selfPool ! DownloadRequest(Listing(sub, sort, count, time, pages, after), receiver, tag)
          }
        }
      }
      case UserComments(user, sort, count, pages, after) => {
        val url = userCommentsUrl(user, sort, count, after)
        try {
          val listing = client.getJSON(url)("data")("children")
          receiver ! CommentListingResult(listing, tag)
          if (pages > 0 && listing.length > 0) {
            selfPool ! DownloadRequest(UserComments(user, sort, count, pages - 1, listing(listing.length - 1)("data")("id")), receiver, tag)
          }
        } catch {
          case jsone: JSONException => {
            Error(s"Error while parsing or traversing JSON retrieved from '$url': " + jsone.getMessage)
          }
          case ioe: IOException => {
            Error(s"Connection error while downloading user comment listing '$url' (requeuing): " + ioe.getMessage)
            Thread.sleep(1000)
            selfPool ! DownloadRequest(UserComments(user, sort, count, pages, after), receiver, tag)
          }
        }
      }
      case Post(id, sort) => {
        val url = s"comments/$id.json?sort=$sort"
        try {
          val data = client.getJSON(url)
          receiver ! PostResult(data, tag)
        } catch {
          case e: JSONException => {
            Error(s"Error while parsing or traversing JSON retrieved from '$url'")
          }
          case ioe: IOException => {
            Error(s"Connection error while downloading post '$url' (requeuing): " + ioe.getMessage)
            Thread.sleep(1000)
            selfPool ! DownloadRequest(Post(id, sort), receiver, tag)
          }
        }
      }
    }
  }

  def subredditUrl(sub: String, sort: PostSort.PostSort = PostSort.Hot, limit: Int = 25, time: String = "", after: String = ""): String = {
    val vars = (if (!time.isEmpty) s"&t=${time}" else "") + (if (after.isEmpty()) "" else s"&after=t3_$after")
    return s"r/$sub/$sort/.json?limit=$limit$vars"
  }

  def userCommentsUrl(user: String, sort: CommentSort.CommentSort = CommentSort.Top, limit: Int = 25, after: String = ""): String = {
    val vars = if (after.isEmpty()) "" else s"&after=t1_$after"
    return s"user/$user/comments/.json?limit=$limit&sort=$sort$vars"
  }
}

object RedditDownloader {
  trait DownloadResult
  case class CommentListingResult(json: JSON, tag: String) extends DownloadResult
  case class PostListingResult(json: JSON, tag: String) extends DownloadResult
  case class PostResult(json: JSON, tag: String) extends DownloadResult

  trait Downloadable
  case class UserComments(user: String, sort: CommentSort.CommentSort, count: Int, pages: Int, after: String = "") extends Downloadable
  case class Listing(subreddit: String, sort: PostSort.PostSort, count: Int, time: String, pages: Int, after: String = "") extends Downloadable
  case class Post(redditId: String, sort: CommentSort.CommentSort) extends Downloadable

  case class DownloadRequest(item: Downloadable, receiver: ActorSelection, tag: String = "") extends WorkCommand
}