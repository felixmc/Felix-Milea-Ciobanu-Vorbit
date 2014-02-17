package com.felixmilea.vorbit.data

import com.felixmilea.vorbit.utils.Initable
import scala.collection.mutable.ArrayBuffer
import com.felixmilea.vorbit.utils.ConfigManager
import com.felixmilea.vorbit.utils.Log
import com.felixmilea.vorbit.utils.Loggable
import com.felixmilea.vorbit.utils.AppUtils

object DBConfig extends Loggable {

  def apply(property: String) = data.getOrElse(property, "")

  val propNames = Seq("host", "database", "username", "password")
  val props = ArrayBuffer[(String, String)]()

  private val data: Map[String, String] = propNames.map(prop => prop -> AppUtils.config("database")(prop).toString).filter(_ != null).toMap

}