package com.felixmilea.vorbit.main

import com.felixmilea.vorbit.utils.Loggable
import com.felixmilea.vorbit.reddit.connectivity.Client
import com.felixmilea.vorbit.reddit.connectivity.RedditUser
import com.felixmilea.vorbit.posting.RedditUserManager
import com.felixmilea.vorbit.reddit.connectivity.Credential

object PosterTest extends App with Loggable {

  val username = "dfdfsd"

  val client = new Client()
  //  if (client.authenticate(new RedditUser(new Credential("123", "")))) {
  val response = client.comment("t3_1odj63", "test")
  Info("comment post response: " + response)
  //  } else {
  //    Error("authentication failed")
  //}

}