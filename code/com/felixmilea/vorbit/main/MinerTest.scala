package com.felixmilea.vorbit.main

import com.felixmilea.vorbit.reddit.connectivity.Credential
import com.felixmilea.vorbit.reddit.connectivity.Client
import scala.util.parsing.json.JSON
import com.felixmilea.vorbit.JSON.JSONTraverser
import com.felixmilea.vorbit.reddit.models.ModelParser
import com.felixmilea.vorbit.data.EntityManager
import com.felixmilea.vorbit.utils.ConfigManager
import com.felixmilea.vorbit.reddit.mining.MinerConfig
import com.felixmilea.vorbit.reddit.mining.Miner
import com.felixmilea.vorbit.data.RedditUserManager
import com.felixmilea.vorbit.utils.Log
import com.felixmilea.vorbit.reddit.connectivity.RedditUser
import com.felixmilea.vorbit.reddit.connectivity.Session
import com.felixmilea.vorbit.data.DBConfig

object MinerTest extends App {

  ConfigManager.init
  //  RedditUserManager.init

  val mineConfig = MinerConfig.parse(ConfigManager("miners")(0))
  val miner = new Miner(mineConfig)
  miner.start()

  //  val user = new RedditUser(new Credential("", ""), new Session(null, null, null))
  //  val client = new Client
  //
  //  if (client.authenticate(user)) {
  //    println(s"Client `${user.credential.username}` authenticated with cookie ${client.session.cookie}")
  //  }

}