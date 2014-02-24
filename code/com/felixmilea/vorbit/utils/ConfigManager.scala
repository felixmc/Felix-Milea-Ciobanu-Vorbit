package com.felixmilea.vorbit.utils

import scala.collection.mutable.HashMap
import java.io.File
import java.io.FilenameFilter
import scala.io.Source
import com.felixmilea.vorbit.data.DBConfig
import com.felixmilea.vorbit.reddit.mining.MinerConfigParser

class ConfigManager(val configDir: String) extends Loggable {
  private[this] val configExt = ".config.json"
  private[this] val configValues = new HashMap[String, JSON]()

  Info("Initializing ConfigManager")

  new File(configDir).listFiles(new FilenameFilter() {
    def accept(dir: File, name: String): Boolean = name.endsWith(configExt)
  }).foreach(file => {
    Debug(s"\t- Loading config file '$file'")
    val configName = file.getName().split("\\.")(0)

    try {
      configValues += (configName -> JSON(Source.fromFile(file).mkString))
    } catch {
      case je: JSONException => Error(s"JSON error while parsing file '$file'")
    }
  })

  val miners = this("miners")
    .filter(c => c("active"))
    .map(c => MinerConfigParser.parse(c))

  lazy val persistence = new ConfigPersistence(this)

  def apply(config: String): JSON = configValues.getOrElse(config, JSON("{}"))
}