package com.felixmilea.vorbit.main

import akka.actor.Props
import com.felixmilea.vorbit.JSON.JSONParser
import com.felixmilea.vorbit.reddit.mining.Miner
import com.felixmilea.vorbit.reddit.mining.MinerConfig
import com.felixmilea.vorbit.utils.ConfigManager
import com.felixmilea.vorbit.utils.App
import com.felixmilea.vorbit.data.DataSetManager
import akka.routing.SmallestMailboxRouter
import com.felixmilea.vorbit.data.TextUnitProcessor
import com.felixmilea.vorbit.data.BigramParser
import com.felixmilea.vorbit.data.TrigramParser
import com.felixmilea.vorbit.data.QuadgramParser
import com.felixmilea.vorbit.reddit.mining.MinerConfigParser

object MinerTest extends App {

  val minerCount = App.config("miners")(JSONParser.L).get.length
  App.actorSystem.actorOf(Props[DataSetManager].withRouter(SmallestMailboxRouter(10 * minerCount)), "DataSetManager")
  App.actorSystem.actorOf(Props[TextUnitProcessor].withRouter(SmallestMailboxRouter(20 * minerCount)), "TextUnitParser")
  App.actorSystem.actorOf(Props[BigramParser].withRouter(SmallestMailboxRouter(10 * minerCount)), "BigramParser")
  App.actorSystem.actorOf(Props[TrigramParser].withRouter(SmallestMailboxRouter(10 * minerCount)), "TrigramParser")
  App.actorSystem.actorOf(Props[QuadgramParser].withRouter(SmallestMailboxRouter(10 * minerCount)), "QuadgramParser")

  // read config file and start all miners
  for (minerIndex <- (0 until minerCount).par) {
    val mineConfig = MinerConfigParser.parse(App.config("miners")(minerIndex))
    val miner = new Miner(mineConfig)
    miner.start()
  }

}