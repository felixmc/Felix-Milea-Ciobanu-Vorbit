package com.felixmilea.vorbit.main

import java.util.Date
import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.ActorSystem
import akka.actor.Props
import com.felixmilea.vorbit.utils.Log
import com.felixmilea.vorbit.data.DBConnection
import com.felixmilea.vorbit.utils.ConfigManager
import akka.routing.SmallestMailboxRouter

object ActorTest extends App {
  class Complex(val a: Int, val b: Int)

  ConfigManager.init

  val system = ActorSystem("MySystem")

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

  Log.Info("start")
  for (i <- (0 to 9).par) {
    var count = 0
    while (count < 100) {
      entityManager ! new Complex(i, count)
      count = count + 1
      Thread.sleep(500)
    }
  }
  Log.Info("end")

}