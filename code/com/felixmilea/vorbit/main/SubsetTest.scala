package com.felixmilea.vorbit.main

import com.felixmilea.vorbit.reddit.mining.SubsetAnalysisManager
import com.felixmilea.vorbit.utils.AppUtils
import com.felixmilea.vorbit.reddit.mining.SubsetMiner

object SubsetTest extends App {
  val dataset = AppUtils.config.persistence.data.datasets("maybeHumanBot")
  val edition = AppUtils.config.persistence.data.editions("symbolWords")
  val subsets = (AppUtils.config.persistence.data.subsets("parents"),
    AppUtils.config.persistence.data.subsets("children"))

  val miner = new SubsetMiner(dataset, subsets, edition)

  miner.start()

  while (true) {
    //    Thread.sleep(5000)
    readLine()
    miner.ping()
  }

}