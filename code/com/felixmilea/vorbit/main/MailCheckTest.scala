package com.felixmilea.vorbit.main

import com.felixmilea.vorbit.utils.Loggable
import com.felixmilea.vorbit.posting.RedditUserManager
import com.felixmilea.vorbit.reddit.connectivity.Client
import com.felixmilea.vorbit.reddit.connectivity.RateLimitException

object MailCheckTest extends App with Loggable {

  val padding = 21

  RedditUserManager.userList.foreach(u => {
    try {
      val client = new Client(u)
      Info(u.credential.username + (" " * (padding - u.credential.username.length)) + " [" + u.id + "]" + (if (u.id < 10) " " else "") + " karma: " + client.commentKarma + "\t\tmail: " + client.hasMail)
    } catch {
      case rte: RateLimitException => {
        Warning(rte.username + " has been rate limited for " + rte.time + " seconds.")
      }
    }

  })

}