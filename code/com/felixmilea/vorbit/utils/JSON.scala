package com.felixmilea.vorbit.utils

import scala.language.dynamics
import scala.util.parsing.json.JSONObject
import scala.util.parsing.json.JSONArray

object JSON {
  def apply(s: String) = new JSON(scala.util.parsing.json.JSON.parseRaw(s))
  def apply(l: List[Any]) = new JSON(new JSONArray(l))

  implicit def ScalaJSONToString(s: JSON) = s.toString
  implicit def ScalaJSONToInt(s: JSON) = s.toInt
  implicit def ScalaJSONToDouble(s: JSON) = s.toDouble
  implicit def ScalaJSONToBool(s: JSON) = s.toBool

  def makeJSON(a: Any): String = a match {
    case m: Map[String, Any] => m.map {
      case (name, content) => makeJSON(name.toString) + ":" + makeJSON(content)
    }.mkString("{", ",", "}")
    case l: List[Any] => l.map(makeJSON).mkString("[", ",", "]")
    case s: String => "\"" + s + "\""
    case i: Int => i.toString
    case d: Double => d.toString
    case b: Boolean => b.toString
    case a: JSONArray => makeJSON(a.list)
    case m: JSONObject => makeJSON(m.obj)
    case j: JSON => makeJSON(j.o)
  }
}

case class JSONException extends Exception

class JSONIterator(i: Iterator[Any]) extends Iterator[JSON] {
  def hasNext = i.hasNext
  def next() = new JSON(i.next())
}

class JSON(a: Any) extends Seq[JSON] with Dynamic {

  val o = a match {
    case op: Option[Any] => op match {
      case Some(jt) => jt
      case None => throw new JSONException
    }
    case _ => a
  }

  override def toString: String = if (o == null) "" else o.toString

  def toInt: Int = o match {
    case i: Integer => i
    case d: Double => d.toInt
    case _ => throw new JSONException
  }

  def toDouble: Double = o match {
    case d: Double => d
    case f: Float => f.toDouble
    case i: Integer => i.toDouble
    case _ => throw new JSONException
  }

  def toBool: Boolean = o match {
    case b: Boolean => b
    case "true" => true
    case "false" => false
    case _ => throw new JSONException
  }

  def apply(key: String): JSON = o match {
    case m: JSONObject => new JSON(m.obj.toMap.get(key))
    case _ => throw new JSONException
  }

  def has(key: String): Boolean = o match {
    case m: JSONObject => new JSON(m.obj.toMap.contains(key))
    case _ => false
  }

  def apply(idx: Int): JSON = o match {
    case a: JSONArray => new JSON(a.list(idx))
    case _ => throw new JSONException
  }

  def length: Int = o match {
    case a: JSONArray => a.list.length
    case m: JSONObject => m.obj.size
    case _ => throw new JSONException
  }

  def iterator: Iterator[JSON] = o match {
    case a: JSONArray => new JSONIterator(a.list.iterator)
    case _ => throw new JSONException
  }

  def selectDynamic(name: String): JSON = apply(name)

  def applyDynamic(name: String)(arg: Any) = {
    arg match {
      case s: String => apply(name)(s)
      case n: Int => apply(name)(n)
      case u: Unit => apply(name)
    }
  }
}