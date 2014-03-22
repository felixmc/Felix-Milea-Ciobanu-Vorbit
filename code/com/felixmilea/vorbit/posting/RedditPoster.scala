package com.felixmilea.vorbit.posting

import akka.actor.Props
import akka.routing.BalancingPool
import com.felixmilea.vorbit.actors.Composer
import com.felixmilea.vorbit.composition.NgramManager
import com.felixmilea.vorbit.utils.JSON
import com.felixmilea.vorbit.utils.Loggable
import com.felixmilea.vorbit.utils.AppUtils
import scala.concurrent.Await
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.duration._
import akka.pattern.AskTimeoutException
import java.util.concurrent.TimeoutException
import com.felixmilea.vorbit.actors.ActorManager._
import akka.actor.ActorRef

class RedditPoster(config: JSON, manager: ActorRef) extends Thread with Loggable {
  Info("Starting RedditPoster '" + config.name + "'")

  val name = config.name.toString

  private[this] val dataset = AppUtils.config.persistence.data.datasets(config.corpus.dataset)
  private[this] val subset = AppUtils.config.persistence.data.subsets(config.corpus.subset)
  private[this] val edition = AppUtils.config.persistence.data.editions(config.corpus.edition)

  private[this] lazy val tasks = {
    var taskId = 0
    config.tasks.map(t => {
      val postingTask = new PostingTask(config, taskId)
      postingTask.start()
      taskId += 1
      postingTask
    })
  }

  override def run() {
    val ngrams = NgramManager(config.corpus.n, dataset, subset, edition)
    val composerActor = AppUtils.actorSystem.actorOf(Props(new Composer(ngrams)).withRouter(BalancingPool(10)), config.name + "-" + PostingManager.Names.composer)
    Debug("   -- Initializing actor " + composerActor.path)

    tasks
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
    tasks.foreach(t => t.gentleStop)
    this.stop()
  }

  def forceStop() {
    tasks.foreach(t => t.forceStop)
    this.stop()
  }

}