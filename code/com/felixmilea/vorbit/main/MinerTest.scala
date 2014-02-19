package com.felixmilea.vorbit.main

import com.felixmilea.vorbit.reddit.mining.MinerConfigParser
import com.felixmilea.vorbit.reddit.mining.RedditMiningManager
import com.felixmilea.vorbit.reddit.mining.RedditMiner
import com.felixmilea.vorbit.utils.AppUtils
import com.felixmilea.vorbit.utils.ConfigPersistence
import com.felixmilea.vorbit.utils.ConfigManager
import com.felixmilea.vorbit.utils.Loggable

object MinerTest extends App with Loggable {
  val manager = new RedditMiningManager(AppUtils.config.miners.length)

  // start miners based on config
  for (minerConfig <- AppUtils.config.miners) {
    try {
      val miner = new RedditMiner(minerConfig, manager)
      miner.start()
    } catch {
      case t: Throwable => {
        Error(s"Unexpected error occured while creating or running miner #$minerConfig: ${t.getMessage}")
      }
    }
  }

  while (true) {
    //    Thread.sleep(5000)
    readLine()
    manager.ping()
  }

}