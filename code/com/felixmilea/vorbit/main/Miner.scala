package com.felixmilea.vorbit.main

import com.felixmilea.vorbit.reddit.Credential
import com.felixmilea.vorbit.reddit.Client
import com.felixmilea.vorbit.reddit.SessionManager

object Miner extends App {

  SessionManager.init

  // get first miner user
  val token = Credential.fromFile("config/miners.config").head

  val client = new Client

  if (client.authenticate(token)) {
    println(s"Client authenticated with cookie ${client.session.cookie}")
    println(s"\nGrabbing client info from reddit..\n${client.getHome}")
  }

  SessionManager.persist
}