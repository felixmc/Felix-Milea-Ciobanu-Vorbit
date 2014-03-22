package com.felixmilea.vorbit.utils

import java.io.File
import java.io.FilenameFilter
import scala.collection.mutable.HashMap
import scala.io.Source
import com.felixmilea.vorbit.data.DBConfig
import com.felixmilea.vorbit.reddit.mining.MinerConfigParser
import com.felixmilea.vorbit.reddit.mining.config.MinerConfig
import java.io.PrintWriter

class ConfigManager(val configDir: String) extends Loggable {
  private[this] val configExt = ".config.json"
  private[this] val configValues = new HashMap[String, JSON]()

  Info("Initializing ConfigManager")

  new File(configDir).listFiles(new FilenameFilter() {
    def accept(dir: File, name: String): Boolean = name.endsWith(configExt)
  }).foreach(file => {
    Debug(s"   -- Loading config file '$file'")
    val configName = file.getName().split("\\.")(0)

    try {
      configValues += (configName -> JSON(Source.fromFile(file).mkString))
    } catch {
      case je: JSONException => Error(s"JSON error while parsing file '$file'")
    }
  })

  private[this] lazy val minerMap: HashMap[String, MinerConfig] =
    new HashMap[String, MinerConfig]() ++
      this("miners")
      .map(c => {
        c.dataset.toString -> MinerConfigParser.parse(c)
      }).toMap

  def getMiner(dataset: Int): Option[MinerConfig] = {
    minerMap.find(m => {
      dataset == persistence.data.datasets(m._1)
    }) match {
      case Some((id, config)) => Some(config)
      case None => None
    }
  }
  def getMiner(dataset: String): Option[MinerConfig] = minerMap.get(dataset)

  def updateMiner(json: JSON) = {
    val miner = MinerConfigParser.parse(json)
    minerMap += (miner.dataset -> miner)

    var found = false
    val miners = apply("miners").map(j => {
      if (j.dataset.toString == json.dataset.toString) {
        found = true
        json
      } else j
    }).toList

    val data = if (found) JSON.makeJSON(miners) else JSON.makeJSON(json :: miners)
    configValues += ("miners" -> JSON(data))
    val writer = new PrintWriter(new File(configDir + "miners" + configExt))
    writer.write(data)
    writer.close()

    loadPer()
  }

  def deleteMiner(dataset: String) = {
    minerMap.remove(dataset)
    val miners = apply("miners").filter(j => j.dataset.toString != dataset)
    val data = JSON.makeJSON(miners)
    configValues += ("miners" -> JSON(data))
    val writer = new PrintWriter(new File(configDir + "miners" + configExt))
    writer.write(data)
    writer.close()
  }

  private[this] lazy val posterMap: HashMap[String, JSON] =
    new HashMap[String, JSON]() ++
      this("posters")
      .map(p => {
        p.name.toString -> p
      }).toMap

  def getPoster(name: String): Option[JSON] = posterMap.get(name)

  def updatePoster(json: JSON) = {
    posterMap += (json.name.toString -> json)

    var found = false
    val posters = apply("posters").map(j => {
      if (j.name.toString == json.name.toString) {
        found = true
        json
      } else j
    }).toList

    val data = if (found) JSON.makeJSON(posters) else JSON.makeJSON(json :: posters)
    configValues += ("posters" -> JSON(data))
    val writer = new PrintWriter(new File(configDir + "posters" + configExt))
    writer.write(data)
    writer.close()
  }

  def deletePoster(name: String) = {
    posterMap.remove(name)
    val posters = apply("posters").filter(j => j.name.toString != name)
    val data = JSON.makeJSON(posters)
    configValues += ("posters" -> JSON(data))
    val writer = new PrintWriter(new File(configDir + "posters" + configExt))
    writer.write(data)
    writer.close()
  }

  def miners = minerMap.values

  private[this] def loadPer() = { per = new ConfigPersistence(this) }
  private[this] var per: ConfigPersistence = null
  def persistence: ConfigPersistence = {
    if (per == null) loadPer
    per
  }

  def apply(config: String): JSON = configValues.getOrElse(config, JSON("{}"))
}