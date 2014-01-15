package com.felixmilea.vorbit.JSON

import scala.util.parsing.json.JSON
import scala.util.parsing.json.JSONObject
import scala.util.parsing.json.JSONArray

object JSONParser {
  def parse(input: String): JSONTraverser =
    new JSONTraverser(JSON.parseRaw(input) match {
      case Some(data) => data match {
        case obj: JSONObject => Some(obj)
        case list: JSONArray => Some(list)
      }
      case _ => None
    })

  abstract class JExtractor[T <: AnyRef] {
    protected var stringParseFunc: ((String) => T) = null

    def unapply(a: AnyRef): Option[T] =
      if (stringParseFunc != null && a.isInstanceOf[String]) unapply(a.asInstanceOf[String])
      else Some(a.asInstanceOf[T])

    def unapply(s: String): Option[T] = if (stringParseFunc != null) {
      try {
        stringParseFunc(s) match {
          case null => None
          case a => Some(a)
        }
      } catch {
        case e: Throwable => None
      }
    } else None
  }

  object M extends JExtractor[Map[String, AnyRef]]
  object L extends JExtractor[List[AnyRef]] {
    override def unapply(a: AnyRef): Option[List[AnyRef]] = {
      if (a.isInstanceOf[JSONArray]) {
        val r = a.asInstanceOf[JSONArray].list.map(item => {
          unapply(item.asInstanceOf[AnyRef])
        })
        Some(r)
      } else {
        super.unapply(a)
      }
    }
  }
  object D extends JExtractor[java.lang.Double] { stringParseFunc = java.lang.Double.parseDouble }
  object I extends JExtractor[java.lang.Integer] {
    override def unapply(a: AnyRef): Option[Integer] =
      D.unapply(a) match {
        case Some(num) => Some(num.toInt)
        case None => None
      }
  }
  object B extends JExtractor[java.lang.Boolean] { stringParseFunc = java.lang.Boolean.parseBoolean }
  object S extends JExtractor[String] {
    override def unapply(a: AnyRef): Option[String] = Some(a.toString)
  }

}