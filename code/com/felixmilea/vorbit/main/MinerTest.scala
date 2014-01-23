package com.felixmilea.vorbit.main

import com.felixmilea.vorbit.JSON.JSONParser
import com.felixmilea.vorbit.reddit.mining.Miner
import com.felixmilea.vorbit.reddit.mining.MinerConfig
import com.felixmilea.vorbit.utils.ConfigManager

object MinerTest extends App {

  ConfigManager.init
  //  RedditUserManager.init

  // read config file and start all miners
  for (minerIndex <- (0 until ConfigManager("miners")(JSONParser.L).get.length).par) {
    val mineConfig = MinerConfig.parse(ConfigManager("miners")(minerIndex))
    val miner = new Miner(mineConfig)
    miner.start()
  }

  while (true) {}

}