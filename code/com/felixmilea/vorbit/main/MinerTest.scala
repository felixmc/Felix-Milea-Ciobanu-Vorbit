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

object MinerTest extends App {

  ConfigManager.init
  RedditUserManager.init

  //  val username = "horsemanboy"

  //  RedditUserManager.getUser(username) match {
  //    case None => Log.Error(s"User $username not found.")
  //    case Some(u) => Log.Info(s"User found > ${u.credential}")
  //  }

  val mineConfig = MinerConfig.parse(ConfigManager("miners")(0))
  //
  val miner = new Miner(mineConfig)
  //
  miner.start()

  //  val user = new RedditUser(new Credential("", ""), new Session(null, null, null))
  //  val client = new Client
  //
  //  if (client.authenticate(user)) {
  //    println(s"Client `${user.credential.username}` authenticated with cookie ${client.session.cookie}")
  //  }

}