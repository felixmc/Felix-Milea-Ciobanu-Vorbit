package com.felixmilea.vorbit.main

import com.felixmilea.vorbit.reddit.connectivity.Credential
import com.felixmilea.vorbit.reddit.connectivity.Client
import com.felixmilea.vorbit.managers.SessionManager
import com.felixmilea.vorbit.managers.SessionManager
import scala.util.parsing.json.JSON
import com.felixmilea.vorbit.JSON.JSONTraverser
import com.felixmilea.vorbit.reddit.models.ModelParser
import com.felixmilea.vorbit.data.EntityManager
import com.felixmilea.vorbit.utils.ConfigManager
import com.felixmilea.vorbit.reddit.mining.MinerConfig
import com.felixmilea.vorbit.reddit.mining.Miner

object Miner extends App {

  SessionManager.init
  ConfigManager.init

  val mineConfig = MinerConfig.parse(ConfigManager("miners")(0))

  val miner = new Miner(mineConfig, 200)

  miner.start()

  //  get first miner user
  //  val token = Credential.fromFile("config/miners.config").head
  //  val client = new Client

  //  client.authenticate(token)

  //  if (client.authenticate(token)) {
  //    println(s"Client authenticated with cookie ${client.session.cookie}")
  //    val postJson = new JSONTraverser(Option(JSON.parseFull(client.get("hot.json")).get.asInstanceOf[AnyRef]))
  //    val postNode = postJson("data")("children")(3)("data")
  //    val post = ModelParser.parse(ModelParser.T3)(postNode)
  //    val commentJson = new JSONTraverser(Option(JSON.parseFull(client.get(s"comments/${post.redditId}.json")).get.asInstanceOf[AnyRef]))
  //    val commentNode = commentJson(1)("data")("children")(0)("data")
  //    val comment = ModelParser.parse(ModelParser.T1)(commentNode)
  //    println(comment)
  //    EntityManager.insertPost(comment)
  //  }

  SessionManager.persist

}