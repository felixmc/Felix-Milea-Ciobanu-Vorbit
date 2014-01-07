package com.felixmilea.vorbit.reddit

import scala.collection.mutable.ArrayBuffer

class ConnectionParameters {
  private val params = new ArrayBuffer[(String, String)]()

  params += "api_type" -> "json"

  def +=(kvPair: (String, String)) = params += kvPair
  def ++=(kvPairs: TraversableOnce[(String, String)]) = params ++= kvPairs

  override def toString(): String = {
    return params.foldRight("")((p, res) => {
      s"${p._1}=${p._2}&$res"
    }).dropRight(1)
  }
}