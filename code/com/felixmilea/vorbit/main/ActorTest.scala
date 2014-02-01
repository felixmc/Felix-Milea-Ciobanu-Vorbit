package com.felixmilea.vorbit.main

import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.ActorSystem
import akka.actor.Props
import com.felixmilea.vorbit.data.DBConnection
import com.felixmilea.vorbit.utils.ConfigManager
import akka.routing.SmallestMailboxRouter
import com.felixmilea.vorbit.utils.ApplicationUtils
import com.felixmilea.vorbit.utils.Loggable

object ActorTest extends App with Loggable {
  class Complex(val a: Int, val b: Int)

  ConfigManager.init

  val system = ApplicationUtils.actorSystem

  class PersistenceActor extends Actor {
    val db = new DBConnection(true)
    val ps = db.conn.prepareStatement("INSERT INTO `complex`(`a`, `b`) VALUES (?,?)")

    def receive = {
      case c: Complex => {
        ps.setInt(1, c.a)
        ps.setInt(2, c.b)
        ps.executeUpdate()
        db.conn.commit()
      }
    }
  }

  val entityManager = system.actorOf(Props[PersistenceActor].withRouter(SmallestMailboxRouter(5)), "router")

  Info("start")
  for (i <- (0 to 9).par) {
    var count = 0
    while (count < 100) {
      entityManager ! new Complex(i, count)
      count = count + 1
      Thread.sleep(500)
    }
  }
  Info("end")

}