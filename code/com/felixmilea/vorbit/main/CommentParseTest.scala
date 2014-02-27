package com.felixmilea.vorbit.main

import com.felixmilea.vorbit.reddit.connectivity.Client
import com.felixmilea.vorbit.posting.RedditUserManager
import com.felixmilea.vorbit.utils.Loggable

object CommentParseTest extends App with Loggable {

  val user = RedditUserManager.usersMap("spidermike23")
  println(user)
  val client = new Client(user)
  client.comment("t3_1sbpym", "beep boop")

}