package com.felixmilea.vorbit.http

import java.net.InetSocketAddress
import akka.io.{ IO, Tcp }
import akka.util.ByteString
import akka.actor.Actor
import akka.actor.Props
import com.felixmilea.vorbit.utils.Loggable

class Server(host: String, port: Int) extends Actor with Loggable {
  import Tcp._
  import context.system

  IO(Tcp) ! Bind(self, new InetSocketAddress(host, port))

  def receive = {
    case b @ Bound(localAddress) => {
      Info("Server bound to " + localAddress)
    }

    case CommandFailed(_: Bind) => context stop self

    case c @ Connected(remote, local) => {
      Info("remote: " + remote)
      val handler = context.actorOf(Props(new HttpHandler(new ApplicationLogicResponder)))
      val connection = sender()
      connection ! Register(handler)
    }
  }

}