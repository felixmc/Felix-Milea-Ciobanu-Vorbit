package com.felixmilea.vorbit.actors

import com.felixmilea.vorbit.reddit.connectivity.RedditUser
import com.felixmilea.vorbit.reddit.connectivity.Client
import com.felixmilea.vorbit.reddit.connectivity.RateLimitException
import com.felixmilea.vorbit.posting.RedditUserManager
import com.felixmilea.vorbit.posting.PostingManager
import com.felixmilea.vorbit.actors.Composer.GeneratedReply
import com.felixmilea.vorbit.utils.JSON
import com.felixmilea.vorbit.actors.PostRecorder.RecordComment
import com.felixmilea.vorbit.reddit.connectivity.TooOldException
import com.felixmilea.vorbit.reddit.connectivity.NoTextException
import com.felixmilea.vorbit.reddit.connectivity.AuthorizationException
import com.felixmilea.vorbit.reddit.connectivity.AuthenticationExcetion
import com.felixmilea.vorbit.reddit.connectivity.RedditUser
import java.io.IOException
import com.felixmilea.vorbit.reddit.connectivity.DeletedLinkException

class Poster(getter: Function0[RedditUser]) extends ManagedActor {
  import Poster._

  private val client: Client = {
    var cl: Client = null
    do {
      try {
        val user = getter.apply()
        Warning("grabbed user: " + user.credential.username + " with session " + user.session.cookie)
        cl = new Client(user)

      } catch {
        case ae: AuthenticationExcetion => {
          Warning(ae.getMessage() + " Requesting new client credentials.")
        }
        case rte: RateLimitException => {
          Warning(rte.username + " has been rate limited and is going to sleep for " + rte.time + " seconds.")
          Thread.sleep((rte.time * 1000).toLong)
        }
      }
    } while (cl == null)
    cl
  }

  private[this] lazy val recorder = sibling(PostingManager.Names.recorder)

  private[this] def user: RedditUser = client.getUser

  def doReceive = {
    case request: Postable => request match {
      case Comment(node, text) => {
        comment(node, text, request)()
      }
      case GeneratedReply(target, text) => {
        comment(target.post.thingId, text, request, true)(result => {
          val json = JSON(result).json.data.things(0).data
          recorder ! RecordComment(target, json.id, text, user.id)
          Debug("comment to '" + target.post.thingId + "' by " + user.credential.username)
          //          client.authenticate(getter.apply())
        })
      }
    }
  }

  private[this] def comment(node: String, text: String, request: Any, proxy: Boolean = false)(callback: Function[String, Unit] = (s) => {}) {
    try {
      val result = client.comment(node, text, proxy)
      callback.apply(result)
    } catch {
      case rte: RateLimitException => {
        Warning(rte.username + " has been rate limited for " + rte.time + " seconds. Comment request requeued to posting pool.")
        Thread.sleep(5000)
        this.selfPool ! request
        //        Thread.sleep((rte.time * 1000).toLong)
      }
      case toe: TooOldException => {
        Warning("'" + toe.path + "' seems to be too old to be commented on. Comment request ignored.")
      }
      case nte: NoTextException => {
        Warning("Comment request to '" + nte.path + "' did not have comment text. Comment request ignored.")
      }
      case ae: AuthorizationException => {
        Warning(user.credential.username + " seems not to be authorized while attempting to comment. Comment request requeued to posting pool and client will attempt re-authorization.")
        this.selfPool ! request
        client.authenticate()
      }
      case dle: DeletedLinkException => {
        Warning("'" + dle.path + "' seems to have been deleted. Comment request ignored.")
      }
      case ioe: IOException => {
        Error("An unexpected connection error was encountered. Comment request requeued to posting pool: " + ioe.getMessage())
        //        Warning("node: " + node + "\ttext: " + text)
        this.selfPool ! request
        Thread.sleep(2000)
      }
    }
  }

}

object Poster {
  trait Postable
  case class Comment(node: String, text: String) extends Postable
}