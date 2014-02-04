package com.felixmilea.vorbit.main

import akka.actor.Props
import com.felixmilea.vorbit.JSON.JSONParser
import com.felixmilea.vorbit.reddit.mining.Miner
import com.felixmilea.vorbit.reddit.mining.MinerConfig
import com.felixmilea.vorbit.utils.ConfigManager
import com.felixmilea.vorbit.utils.ApplicationUtils
import com.felixmilea.vorbit.data.DataSetManager
import akka.routing.SmallestMailboxRouter
import com.felixmilea.vorbit.data.NGramParser
import com.felixmilea.vorbit.data.BigramParser

object MinerTest extends App {

  ConfigManager.init

  val minerCount = ConfigManager("miners")(JSONParser.L).get.length
  val postEntityManager = ApplicationUtils.actorSystem.actorOf(Props[DataSetManager].withRouter(SmallestMailboxRouter(5 * minerCount)), "PostEntityManager")
  ApplicationUtils.actorSystem.actorOf(Props[NGramParser].withRouter(SmallestMailboxRouter(10 * minerCount)), "NgramParser")
  ApplicationUtils.actorSystem.actorOf(Props[BigramParser].withRouter(SmallestMailboxRouter(10 * minerCount)), "BigramParser")

  // read config file and start all miners
  for (minerIndex <- (0 until minerCount).par) {
    val mineConfig = MinerConfig.parse(ConfigManager("miners")(minerIndex))
    val miner = new Miner(mineConfig, postEntityManager)
    miner.start()
  }

}