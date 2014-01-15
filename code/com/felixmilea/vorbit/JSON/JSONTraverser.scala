package com.felixmilea.vorbit.JSON

import scala.util.parsing.json.JSONArray
import scala.util.parsing.json.JSONObject

class JSONTraverser(val data: Option[AnyRef]) {

  def apply[T <: AnyRef](index: String): JSONTraverser = return new JSONTraverser(apply[T](Right(index)))
  def apply[T <: AnyRef](index: Int): JSONTraverser = return new JSONTraverser(apply[T](Left(index)))

  def apply[T <: AnyRef](extractor: JSONParser.JExtractor[T] = JSONParser.S): Option[T] =
    data match {
      case Some(data) => extractor.unapply(data)
      case None => None
    }

  def apply[T](index: Either[Int, String]): Option[T] =
    try {
      (data, index) match {
        case (Some(data), Left(i)) =>
          if (data.isInstanceOf[JSONArray]) return Some(data.asInstanceOf[JSONArray].list(i).asInstanceOf[T])
          else if (data.isInstanceOf[List[T]]) return Some(data.asInstanceOf[List[T]](i))
        case (Some(data), Right(s)) =>
          if (data.isInstanceOf[JSONObject]) return Some(data.asInstanceOf[JSONObject].obj(s).asInstanceOf[T])
          else if (data.isInstanceOf[Map[String, T]]) return Some(data.asInstanceOf[Map[String, T]](s))
        case _ => None
      }
      return None
    } catch {
      // return none if element not found
      case _: NoSuchElementException => None
    }

  override def toString(): String = return data.toString
}