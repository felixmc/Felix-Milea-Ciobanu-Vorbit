package com.felixmilea.vorbit.reddit.mining

import java.util.Date
import akka.actor.ActorRef
import com.felixmilea.vorbit.reddit.mining.config.MinerConfig
import com.felixmilea.vorbit.reddit.mining.config.TaskConfig
import com.felixmilea.vorbit.data.DBConnection
import com.felixmilea.vorbit.utils.AppUtils
import com.felixmilea.vorbit.utils.Loggable
import scala.concurrent.Await
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.duration._
import akka.pattern.AskTimeoutException
import java.util.concurrent.TimeoutException

class RedditMiner(config: MinerConfig, manager: ActorRef) extends Thread with Loggable {
  import com.felixmilea.vorbit.actors.ActorManager._

  val id = AppUtils.config.persistence.data.datasets(config.dataset)
  private[this] lazy val db = new DBConnection(true)
  db.conn.setAutoCommit(true)
  private[this] lazy val getLastPerformed = db.conn.prepareCall("SELECT `last_performed` FROM `dataset_mining_tasks` WHERE `dataset` = ? AND `name` = ? LIMIT 1")

  Info("Starting RedditMiner '" + config.dataset + "'")

  private[this] lazy val tasks = config.tasks.map(task => {
    getLastPerformed.setInt(1, AppUtils.config.persistence.data.datasets(config.dataset))
    getLastPerformed.setString(2, task.name)
    val rows = getLastPerformed.executeQuery()
    val hasLast = rows.next()
    val lastPerformed = if (hasLast) rows.getDate(1) else null

    if (lastPerformed == null || (task.recurrence != 0 && (lastPerformed.getTime + task.recurrence) < new Date().getTime)) {
      val taskConfig = TaskConfig(config.dataset, task)
      val miningTask = new MiningTask(taskConfig, manager)
      miningTask.start()
      Some(miningTask)
    } else {
      None
    }
  })

  override def run() {
    // make reference to tasks in order to run lazy load instanciation
    tasks

    db.conn.close()
  }

  def getStatus(): Int = {
    manager ! PingChildren

    Thread.sleep(100)

    try {
      implicit val timeout = Timeout(3 seconds)
      val future = manager ? PingStatus
      val result = Await.result(future, timeout.duration).asInstanceOf[Any]

      result match {
        case Done(date, duration) => {
          Debug("duration: " + duration)
          if (duration <= 10) return 0
          else return 1
        }
        case NotDone => {
          return 1
        }
      }
    } catch {
      case ate: AskTimeoutException => return 1
      case te: TimeoutException => return 1
    }
  }

  def gentleStop() {
    tasks.foreach(t => t match {
      case Some(task) => task.gentleStop
      case None => {}
    })

    this.stop()
  }

  def forceStop() {
    tasks.foreach(t => t match {
      case Some(task) => task.forceStop
      case None => {}
    })

    this.stop()
  }

}