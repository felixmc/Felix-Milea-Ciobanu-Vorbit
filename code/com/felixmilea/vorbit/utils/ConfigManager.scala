package com.felixmilea.vorbit.utils

import scala.collection.mutable.HashMap
import java.io.File
import java.io.FilenameFilter
import scala.io.Source
import com.felixmilea.vorbit.JSON.JSONParser
import scala.util.parsing.json.JSONObject
import com.felixmilea.vorbit.JSON.JSONTraverser
import com.felixmilea.vorbit.data.DBConfig

object ConfigManager extends Initable {
  private val configDir = "config/"
  private val configExt = ".config.json"
  private val configValues = new HashMap[String, JSONTraverser]()
  private val configs = Seq[Initable](DBConfig)

  def init() {
    Log.Info("Initializing ConfigManager")

    new File(configDir).listFiles(new FilenameFilter() {
      def accept(dir: File, name: String): Boolean = name.endsWith(configExt)
    }).foreach(file => {
      Log.Debug(s"\t- Loading config file `$file`")
      val configName = file.getName().split("\\.")(0)

      val jt = JSONParser.parse(Source.fromFile(file).mkString)

      jt.data match {
        case Some(data) => configValues += (configName -> jt)
        case None => Log.Warning(s"\t- Could not parse config file `$file`")
      }
    })

    configs.foreach { _.init }
  }

  def apply(config: String): JSONTraverser = configValues.getOrElse(config, new JSONTraverser(None))

}