package com.felixmilea.vorbit.utils

import com.felixmilea.vorbit.reddit.mining.MinerConfigParser
import com.felixmilea.vorbit.reddit.mining.config.MinerConfig
import com.felixmilea.vorbit.data.DBConnection
import com.felixmilea.vorbit.data.ResultSetIterator

class ConfigPersistence(config: ConfigManager) {
  private[this] val db = new DBConnection(true)
  db.conn.setAutoCommit(true)

  private[this] lazy val createEdition = db.conn.prepareCall("{CALL create_edition(?,?)}")
  private[this] lazy val createSubset = db.conn.prepareCall("{CALL create_subset(?,?)}")
  private[this] lazy val createDataset = db.conn.prepareCall("{CALL create_dataset(?,?)}")
  private[this] lazy val getDatasets = db.conn.prepareStatement("SELECT * FROM `datasets`")
  private[this] lazy val createMiningTask = db.conn.prepareCall("{CALL create_mining_task(?, ?)}")

  val data: DataConfig = {
    val setupData = config("database")("setup")("data")

    // setup subsets
    val subsets = setupData("subsets").map(s => {
      val name = s.toString
      createSubset.setString(1, name)
      createSubset.registerOutParameter(2, java.sql.Types.INTEGER)
      createSubset.execute()
      name -> createSubset.getInt(2)
    }).toMap

    // setup editions
    val editions = setupData("editions").map(e => {
      val name = e.toString
      createEdition.setString(1, name)
      createEdition.registerOutParameter(2, java.sql.Types.INTEGER)
      createEdition.execute()
      name -> createEdition.getInt(2)
    }).toMap

    val curDatasets = {
      ResultSetIterator(getDatasets.executeQuery()).map(r => {
        r.getString("name") -> r.getInt("id")
      }).toMap
    }

    config.miners.filterNot(mc => curDatasets.contains(mc.dataset)).foreach(mc => (mc.dataset, registerMiner(mc)))

    val datasets = {
      ResultSetIterator(getDatasets.executeQuery()).map(r => {
        r.getString("name") -> r.getInt("id")
      }).toMap
    }

    db.conn.close()

    DataConfig(datasets, subsets, editions)
  }

  private[this] def registerMiner(miner: MinerConfig): Int = {
    // create dataset
    createDataset.setString(1, miner.dataset)
    createDataset.registerOutParameter(2, java.sql.Types.INTEGER)
    createDataset.execute()
    val datasetId = createDataset.getInt(2)

    // register tasks
    miner.tasks.foreach(task => {
      createMiningTask.setInt(1, datasetId)
      createMiningTask.setString(2, task.name)
      createMiningTask.execute()
    })

    datasetId
  }

}

case class DataConfig(datasets: Map[String, Int], subsets: Map[String, Int], editions: Map[String, Int])