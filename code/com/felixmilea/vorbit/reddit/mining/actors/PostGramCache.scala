package com.felixmilea.vorbit.reddit.mining.actors

import scala.collection.mutable.HashMap
import akka.actor.ActorSelection

class PostGramCache extends ManagedActor {
  import PostGramCache._

  private[this] val cacheData = new HashMap[Tuple3[Int, Int, String], Tuple2[Int, Seq[Int]]]

  def doReceive = {
    case Put(key, value) => {
      cacheData.put(key, value)
    }
    case Get(key, receiver) => {
      if (receiver == null) sender ! Entry(key, cacheData.get(key))
      else receiver ! Entry(key, cacheData.get(key))
    }
  }

}

object PostGramCache {
  case class Get(key: Tuple3[Int, Int, String], receiver: ActorSelection = null)
  case class Put(key: Tuple3[Int, Int, String], value: Tuple2[Int, Seq[Int]])
  case class Entry(key: Tuple3[Int, Int, String], value: Option[Tuple2[Int, Seq[Int]]])
}