package com.felixmilea.vorbit.actors

import com.felixmilea.vorbit.reddit.connectivity.RedditUser
import com.felixmilea.vorbit.reddit.connectivity.Client
import com.felixmilea.vorbit.reddit.connectivity.RateLimitException
import com.felixmilea.vorbit.posting.RedditUserManager

class Poster(user: RedditUser = RedditUserManager.grabNext()) extends ManagedActor {
  import Poster._

  private lazy val client = new Client(user)

  def doReceive = {
    case Comment(node, text) => {
      try {
        client.comment(node, text)
      } catch {
        case rt: RateLimitException => {
          Error(rt.username + " has been rate limited and is going to sleep for " + rt.time + " seconds.")
          Thread.sleep((rt.time * 1000).toLong)
        }
      }
    }
  }

}

object Poster {
  case class Comment(node: String, text: String)
}