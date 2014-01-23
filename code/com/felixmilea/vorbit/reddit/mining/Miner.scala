package com.felixmilea.vorbit.reddit.mining

import com.felixmilea.scala.profiling.FunctionTimer
import com.felixmilea.vorbit.data.EntityManager
import com.felixmilea.vorbit.utils.Log

class Miner(private val config: MinerConfig) extends Thread {
  private val DELAY = (1000 * 60) * 5
  private val engine = MiningEngine.get(config)

  EntityManager.setupMiner(config.name)

  override def run() {
    val ft = new FunctionTimer
    while (true) {
      Log.Info(s"Starting data mining operation `${config.name}`")
      ft { engine.mine }
      Log.Info(s"Data mining operation `${config.name}` completed in ${ft.elapsedTime} ms")
      Log.Info(s"Resuming data mining operation `${config.name}` in $DELAY ms")
      Thread.sleep(DELAY)
    }
  }

}