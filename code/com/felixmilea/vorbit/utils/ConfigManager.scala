package com.felixmilea.vorbit.utils

import scala.collection.mutable.HashMap
import java.io.File
import java.io.FilenameFilter
import scala.io.Source
import com.felixmilea.vorbit.JSON.JSONParser
import scala.util.parsing.json.JSONObject

object ConfigManager {
  private val configDir = "config/"
  private val configExt = ".config.json"
  private val configValues = new HashMap[String, Map[String, String]]()

  def init() {
    Log.Info("ConfigManager initializing..")

    new File(configDir).listFiles(new FilenameFilter() {
      def accept(dir: File, name: String): Boolean = name.endsWith(configExt)
    }).foreach(file => {
      Log.Debug(s"\t- Loading config file `$file`..")
      val configName = file.getName().split("\\.")(0)

      JSONParser.parse(Source.fromFile(file).mkString).data match {
        case Some(data) => configValues += (configName -> data.asInstanceOf[JSONObject].obj.asInstanceOf[Map[String, String]])
        case None => Log.Warning(s"\t- Could not parse config file `$file`")
      }
    })
  }

  def apply(config: String)(key: String): String = configValues(config)(key)

}