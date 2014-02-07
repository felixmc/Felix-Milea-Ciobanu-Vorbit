package com.felixmilea.vorbit.main

import akka.actor.Props
import com.felixmilea.vorbit.JSON.JSONParser
import com.felixmilea.vorbit.reddit.mining.Miner
import com.felixmilea.vorbit.reddit.mining.MinerConfig
import com.felixmilea.vorbit.utils.ConfigManager
import com.felixmilea.vorbit.utils.ApplicationUtils
import com.felixmilea.vorbit.data.DataSetManager
import akka.routing.SmallestMailboxRouter
import com.felixmilea.vorbit.data.TextUnitProcessor
import com.felixmilea.vorbit.data.BigramParser
import com.felixmilea.vorbit.data.TrigramParser
import com.felixmilea.vorbit.data.QuadgramParser

object MinerTest extends App {

  val minerCount = ApplicationUtils.config("miners")(JSONParser.L).get.length
  ApplicationUtils.actorSystem.actorOf(Props[DataSetManager].withRouter(SmallestMailboxRouter(10 * minerCount)), "DataSetManager")
  ApplicationUtils.actorSystem.actorOf(Props[TextUnitProcessor].withRouter(SmallestMailboxRouter(20 * minerCount)), "TextUnitParser")
  ApplicationUtils.actorSystem.actorOf(Props[BigramParser].withRouter(SmallestMailboxRouter(10 * minerCount)), "BigramParser")
  ApplicationUtils.actorSystem.actorOf(Props[TrigramParser].withRouter(SmallestMailboxRouter(10 * minerCount)), "TrigramParser")
  ApplicationUtils.actorSystem.actorOf(Props[QuadgramParser].withRouter(SmallestMailboxRouter(10 * minerCount)), "QuadgramParser")

  // read config file and start all miners
  for (minerIndex <- (0 until minerCount).par) {
    val mineConfig = MinerConfig.parse(ApplicationUtils.config("miners")(minerIndex))
    val miner = new Miner(mineConfig)
    miner.start()
  }

}