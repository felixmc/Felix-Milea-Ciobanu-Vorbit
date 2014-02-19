package com.felixmilea.vorbit.main

import com.felixmilea.vorbit.utils.Loggable
import com.felixmilea.vorbit.reddit.connectivity.Client
import com.felixmilea.vorbit.reddit.connectivity.RedditUser
import com.felixmilea.vorbit.data.RedditUserManager

object PosterTest extends App with Loggable {

  val username = "dfdfsd"

  RedditUserManager.getUser(username) match {
    case Some(user) => {
      val client = new Client()
      if (client.authenticate(user)) {
        val response = client.comment("t3_1mcvti", "test")
        Info("comment post response: " + response)
      } else {
        Error("authentication failed")
      }
    }
    case None => {
      Error(s"User '$username' was not found.")
    }
  }

}