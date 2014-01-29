package com.felixmilea.vorbit.reddit.mining

import com.felixmilea.scala.profiling.FunctionTimer
import com.felixmilea.vorbit.data.EntityManager
import com.felixmilea.vorbit.utils.Log
import com.felixmilea.vorbit.utils.Loggable

class Miner(private val config: MinerConfig) extends Thread with Loggable {
  private val DELAY = (1000 * 60) * 5
  private val engine = MiningEngine.get(config)

  EntityManager.setupMiner(config.name)

  override def run() {
    val ft = new FunctionTimer
    while (true) {
      Info(s"Starting data mining operation `${config.name}`")
      ft { engine.mine }
      Info(s"Data mining operation `${config.name}` completed in ${ft.elapsedTime} ms")
      Info(s"Resuming data mining operation `${config.name}` in $DELAY ms")
      Thread.sleep(DELAY)
    }
  }

}