package com.felixmilea.vorbit.main

import com.felixmilea.vorbit.reddit.Credential
import com.felixmilea.vorbit.reddit.ConnectionParameters
import com.felixmilea.vorbit.reddit.Client

object Miner extends App {

  // get first miner user
  val token = Credential.fromFile("config/miners.config").head

  val client = new Client

  if (client.authenticate(token)) {
    println(s"Client authenticated with cookie ${client.cookie}")
  }

}