package com.felixmilea.vorbit.main

import akka.actor.ActorSystem
import com.felixmilea.vorbit.utils.Initable
import com.felixmilea.vorbit.utils.ConfigManager
import com.felixmilea.vorbit.utils.Loggable

object Vorbit extends App with Loggable {

  Info("Vorbit running")

  override def wrapLog(m: String) = "VB: " + m

}