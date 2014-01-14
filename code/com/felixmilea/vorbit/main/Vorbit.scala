package com.felixmilea.vorbit.main

import com.felixmilea.vorbit.utils.Log
import com.felixmilea.vorbit.utils.ConfigManager
import com.felixmilea.vorbit.utils.Initable
import com.felixmilea.vorbit.data.Setup
import com.felixmilea.scala.console.ConsoleMenuApp
import com.felixmilea.scala.console.MenuItem

object Vorbit extends ConsoleMenuApp {

  start()

  override def start() {
    Log.init()
    Log.Info("Vorbit started.")
    Seq[Initable](ConfigManager, Setup).foreach(i => i.init)

    setupMenu()

    super.start
  }

  def setupMenu() {
    menu.title = "Vorbit v0.1"
    menu.addItem(new MenuItem("Do Something", something))
  }

  def something() {
    println("Doing something..")
    Log.Info("Vorbit is doing something")
  }

}