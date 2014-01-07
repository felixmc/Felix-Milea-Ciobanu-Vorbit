package com.felixmilea.vorbit.main

import com.felixmilea.vorbit.reddit.Credential
import com.felixmilea.vorbit.reddit.ConnectionParameters

object Miner extends App {

  // get first miner user
  val user = Credential.fromFile("config/miners.config").head

  val params = new ConnectionParameters
  params += ("persist", "true")

  println(params)
}