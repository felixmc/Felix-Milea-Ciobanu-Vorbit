package com.felixmilea.vorbit.http

import java.net.InetSocketAddress
import akka.io.{ IO, Tcp }
import akka.util.ByteString
import akka.actor.Actor
import akka.actor.Props
import com.felixmilea.vorbit.utils.Loggable
import com.felixmilea.vorbit.application.ApplicationRoot

class Server(host: String, port: Int) extends Actor with Loggable {
  import Tcp._
  import context.system

  IO(Tcp) ! Bind(self, new InetSocketAddress(host, port))

  def receive = {
    case b @ Bound(localAddress) => {
      Info("Server bound to " + localAddress)
    }

    case CommandFailed(_: Bind) => {
      Fatal("Port '" + port + "' is already in use.")
      context stop self
    }

    case c @ Connected(remote, local) => {
      Info("remote: " + remote)
      val handler = context.actorOf(Props(new HttpHandler(new ApplicationRoot)))
      val connection = sender()
      connection ! Register(handler)
    }
  }

}