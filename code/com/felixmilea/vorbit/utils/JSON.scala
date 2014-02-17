package com.felixmilea.vorbit.utils

import scala.language.dynamics
import scala.util.parsing.json.JSONObject
import scala.util.parsing.json.JSONArray

object JSON {
  def apply(s: String) = new JSON(scala.util.parsing.json.JSON.parseRaw(s))
  implicit def ScalaJSONToString(s: JSON) = s.toString
  implicit def ScalaJSONToInt(s: JSON) = s.toInt
  implicit def ScalaJSONToDouble(s: JSON) = s.toDouble
  implicit def ScalaJSONToBool(s: JSON) = s.toBool
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

  override def toString: String = o.toString

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
    case b: java.lang.Boolean => b
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