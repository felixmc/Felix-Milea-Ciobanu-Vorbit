package com.felixmilea.vorbit.actors

import com.felixmilea.vorbit.data.DBConnection
import com.felixmilea.vorbit.utils.AppUtils

class TaskRecorder extends ManagedActor {
  import TaskRecorder._

  private[this] val db = new DBConnection(true)
  private[this] lazy val updateTask = db.conn.prepareCall("{CALL update_mining_task(?,?)}")

  def doReceive = {
    case UpdateTask(dataset, name) => {
      val datasetId = AppUtils.config.persistence.data.datasets(dataset)
      updateTask.setInt(1, datasetId)
      updateTask.setString(2, name)
      updateTask.execute()
      db.conn.commit()
    }
  }

}

object TaskRecorder {
  case class UpdateTask(dataset: String, name: String)
}