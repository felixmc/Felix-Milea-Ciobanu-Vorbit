package com.felixmilea.vorbit.application

import akka.actor.Props
import com.felixmilea.vorbit.utils.AppUtils
import com.felixmilea.vorbit.utils.Loggable
import com.felixmilea.vorbit.http.Server

object Vorbit extends App with Loggable {
  val runConfig = AppUtils.config.apply("run")
  val httpServer = AppUtils.actorSystem.actorOf(Props(new Server(runConfig.host, runConfig.port)))
}