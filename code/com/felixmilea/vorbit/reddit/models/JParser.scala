package com.felixmilea.vorbit.reddit.models

import scala.util.parsing.json.JSON

object JParser {

  def parse(input: String, strategy: (Some[Any]) => List[Map[String, AnyRef]]): List[Map[String, AnyRef]] =
    JSON.parseFull(input) match {
      case Some(data) => strategy(Some(data))
      case _ => null
    }

  class CC[T] { def unapply(a: Any): Option[T] = Some(a.asInstanceOf[T]) }
  object M extends CC[Map[String, Any]]
  object L extends CC[List[Any]]
  object S extends CC[String]
  object D extends CC[Double]
  object B extends CC[Boolean]

  object Strategy {
    def login(input: Some[Any]): List[Map[String, AnyRef]] = {
      for {
        Some(M(map)) <- List(input)
        M(json) = map("json")
        L(errors) = json("errors")
        M(data) = json.getOrElse("data", Map())
        S(modhash) = data.getOrElse("modhash", null)
        S(cookie) = data.getOrElse("cookie", null)
      } yield {
        Seq(("errors" -> errors),
          ("modhash" -> modhash),
          ("cookie" -> cookie)).toMap
      }
    }

    def t1(input: Some[Any]): List[Map[String, AnyRef]] = {
      return null
    }

    def t2(input: Some[Any]): List[Map[String, AnyRef]] = {
      return null
    }

    def t3(input: Some[Any]): List[Map[String, AnyRef]] = {
      return null
    }

    def t4(input: Some[Any]): List[Map[String, AnyRef]] = {
      return null
    }

    def t5(input: Some[Any]): List[Map[String, AnyRef]] = {
      return null
    }

    def t6(input: Some[Any]): List[Map[String, AnyRef]] = {
      return null
    }
  }

}