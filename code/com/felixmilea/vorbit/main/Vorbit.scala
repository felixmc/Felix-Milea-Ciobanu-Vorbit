package com.felixmilea.vorbit.main

import akka.actor.ActorSystem
import com.felixmilea.vorbit.utils.Initable
import com.felixmilea.vorbit.utils.ConfigManager
import com.felixmilea.vorbit.utils.Loggable
import com.felixmilea.vorbit.utils.ApplicationUtils

object Vorbit extends App with Loggable {
  private val modules = Seq[Initable](ConfigManager)

  Info("Vorbit running")

  init()
  run()

  def init() {
    modules.foreach { _.init }
  }

  def run() {

  }

}