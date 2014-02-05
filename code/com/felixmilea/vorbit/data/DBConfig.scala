package com.felixmilea.vorbit.data

import com.felixmilea.vorbit.utils.Initable
import scala.collection.mutable.ArrayBuffer
import com.felixmilea.vorbit.utils.ConfigManager
import com.felixmilea.vorbit.utils.Log
import com.felixmilea.vorbit.utils.Loggable
import com.felixmilea.vorbit.utils.ApplicationUtils

object DBConfig extends Loggable {
  private var data: Map[String, String] = Map()

  def apply(property: String) = data.getOrElse(property, "")

  val propNames = Seq("host", "database", "username", "password", "mined_data_table_prefix")
  val props = ArrayBuffer[(String, String)]()

  propNames.foreach(prop =>
    (ApplicationUtils.config("database")(prop)()) match {
      case Some(value) => props += prop -> value
      case None => Warning(s"Could not load database config property `$prop` from ConfigManager.")
    })

  data = props.toMap
}