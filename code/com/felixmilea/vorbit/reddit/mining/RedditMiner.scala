package com.felixmilea.vorbit.reddit.mining

import java.util.Date
import com.felixmilea.vorbit.reddit.mining.config.MinerConfig
import com.felixmilea.vorbit.reddit.mining.config.TaskConfig
import com.felixmilea.vorbit.data.DBConnection
import com.felixmilea.vorbit.utils.AppUtils

class RedditMiner(config: MinerConfig, man: MiningManager) extends Miner {
  val manager = man

  private[this] lazy val db = new DBConnection(true)
  db.conn.setAutoCommit(true)
  private[this] lazy val getLastPerformed = db.conn.prepareCall("SELECT `last_performed` FROM `dataset_mining_tasks` WHERE `dataset` = ? AND `name` = ? LIMIT 1")

  override def run() {
    for (task <- config.tasks) {
      getLastPerformed.setInt(1, AppUtils.config.persistence.data.datasets(config.dataset))
      getLastPerformed.setString(2, task.name)
      val rows = getLastPerformed.executeQuery()
      rows.next()
      val lastPerformed = rows.getDate(1)

      if (lastPerformed == null || (task.recurrence != 0 && (lastPerformed.getTime + task.recurrence) < new Date().getTime)) {
        val taskConfig = TaskConfig(config.dataset, task)
        val miningTask = new MiningTask(taskConfig, manager)
        miningTask.start()
      }
    }

    db.conn.close()
  }

}