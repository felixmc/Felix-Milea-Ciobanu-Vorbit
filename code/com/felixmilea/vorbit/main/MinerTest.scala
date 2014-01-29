package com.felixmilea.vorbit.main

import akka.actor.Props
import com.felixmilea.vorbit.JSON.JSONParser
import com.felixmilea.vorbit.reddit.mining.Miner
import com.felixmilea.vorbit.reddit.mining.MinerConfig
import com.felixmilea.vorbit.utils.ConfigManager
import com.felixmilea.vorbit.utils.ApplicationUtils
import com.felixmilea.vorbit.data.DataSetManager

object MinerTest extends App {

  ConfigManager.init

  val postEntityManager = ApplicationUtils.getActorSystem.actorOf(Props[DataSetManager], "PostEntityManager")

  // read config file and start all miners
  for (minerIndex <- (0 until ConfigManager("miners")(JSONParser.L).get.length).par) {
    val mineConfig = MinerConfig.parse(ConfigManager("miners")(minerIndex))
    val miner = new Miner(mineConfig, postEntityManager)
    miner.start()
  }

}