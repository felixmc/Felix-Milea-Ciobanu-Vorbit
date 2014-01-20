package com.felixmilea.vorbit.main

import com.felixmilea.vorbit.utils.Log
import com.felixmilea.vorbit.utils.ConfigManager
import com.felixmilea.vorbit.utils.Initable
import com.felixmilea.scala.console.ConsoleMenuApp
import com.felixmilea.scala.console.MenuItem
import com.felixmilea.scala.console.ConsoleMenu
import com.felixmilea.vorbit.JSON.JSONParser
import com.felixmilea.vorbit.reddit.mining.Miner
import com.felixmilea.vorbit.reddit.mining.MinerConfig

object Vorbit extends ConsoleMenuApp {

  start()

  override def start() {
    Log.init()
    Log.Info("Vorbit started.")
    Seq[Initable](ConfigManager).foreach { _.init }

    setupMenu()

    super.start
  }

  def setupMenu() {
    menu.title = "Vorbit v0.1"
    menu.addItem(new MenuItem("Start A Miner", miners))
  }

  def miners() {
    val mm = new ConsoleMenu("MinerMenu")

    def quit(miner: Miner) {
      miner.stop();
    }

    for (i <- 0 until ConfigManager("miners")(JSONParser.L).get.length) {
      val minerConfig = MinerConfig.parse(ConfigManager("miners")(i))
      mm.addItem(new MenuItem(minerConfig.name, () => {
        val miner = new Miner(minerConfig)
        miner.start()
        println("Press enter to stop miner..")
        Console.readLine()
        quit(miner)
      }))
    }

    mm.addItem(new MenuItem("Go back", () => {}))

    mm.promptMenu()

  }

}