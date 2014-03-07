package com.felixmilea.vorbit.main

import akka.actor.Props
import akka.util.ByteString
import akka.io.{ IO, Tcp }
import com.felixmilea.vorbit.utils.AppUtils
import com.felixmilea.vorbit.http.Server
import com.felixmilea.vorbit.utils.Loggable
import com.felixmilea.vorbit.http.Response
import com.felixmilea.vorbit.http.Util._

object ServerTest extends App with Loggable {
  import Tcp._

  val runConfig = AppUtils.config.apply("run")
  val httpServer = AppUtils.actorSystem.actorOf(Props(new Server(runConfig.host, runConfig.port)))
}