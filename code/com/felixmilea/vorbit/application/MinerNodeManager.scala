package com.felixmilea.vorbit.application

import akka.actor.Props
import com.felixmilea.vorbit.http.ChildNode
import com.felixmilea.vorbit.http.Response
import com.felixmilea.vorbit.http.Util.Request
import com.felixmilea.vorbit.http.Util.Status
import com.felixmilea.vorbit.utils.AppUtils
import com.felixmilea.vorbit.utils.JSON
import com.felixmilea.vorbit.http.Util.Header
import com.felixmilea.vorbit.utils.Loggable
import com.felixmilea.vorbit.http.EmbeddedChildNode
import com.felixmilea.vorbit.http.NamedChildNode
import com.felixmilea.vorbit.reddit.mining.RedditMiningManager
import com.felixmilea.vorbit.reddit.mining.RedditMiner
import com.felixmilea.vorbit.reddit.mining.MinerConfigParser
import com.felixmilea.vorbit.utils.JSONException
import com.felixmilea.vorbit.reddit.mining.config.MinerConfigParsingException

class MinerNodeManager(name: String) extends DataListingNode(name) with Loggable {
  import MinerNodeManager._

  private[this] val kids = Seq(StartNode, StopNode, StatusNode, UpdateNode, DeleteNode, ConfigNode)
  override def children: Seq[ChildNode] = kids

  override protected[this] def data: Map[String, Int] = AppUtils.config.miners.map(p => {
    val name = p.dataset
    (name -> AppUtils.config.persistence.data.datasets(name))
  }).toMap

}

object MinerNodeManager extends Loggable {
  val managerName = "MiningManager"
  lazy val manager = AppUtils.actorSystem.actorOf(Props(new RedditMiningManager(1)), managerName)

  var miner: RedditMiner = null

  object StartNode extends NamedChildNode("start") {
    val children = Seq()

    def execute(req: Request, segment: String): Response = {
      if (miner != null) return Response(Status(400), "A miner is already running.")

      val id = req.query.find(p => p.name == "miner") match {
        case Some(minerId) => minerId.value.toInt
        case None => return Response(Status(400), "No miner id provided.")
      }

      val minerConfig = AppUtils.config.getMiner(id) match {
        case Some(mc) => mc
        case None => return Response(Status(400), "No miner was found with id '" + id + "'.")
      }

      try {
        miner = new RedditMiner(minerConfig, manager)
        miner.start()
        return Response(Status(200), "Miner started.")
      } catch {
        case t: Throwable => {
          Error(s"Unexpected error occured while creating or running miner #${minerConfig.dataset}: ${t.getMessage}")
          return Response(Status(500), "Error starting miner.")
        }
      }
    }
  }

  object StopNode extends NamedChildNode("stop") {
    val children = Seq()

    def execute(req: Request, segment: String): Response = {
      if (miner == null) return Response(Status(500), "No miner currently running.")

      miner.getStatus match {
        case 0 => miner.gentleStop
        case 1 => miner.forceStop
      }

      miner = null
      return Response(Status(200), "Miner stopped.")
    }
  }

  object StatusNode extends NamedChildNode("status") {
    val children = Seq()

    def execute(req: Request, segment: String): Response = {
      if (miner == null)
        return Response(Status(200), "{'active': false}", Seq(Header.Content.json))
      else
        return Response(Status(200), "{'active': true, 'miner': " + miner.id + ", 'status': " + miner.getStatus + "}", Seq(Header.Content.json))
    }
  }

  object UpdateNode extends NamedChildNode("update") {
    val children = Seq()

    def execute(req: Request, segment: String): Response = {
      if (miner != null)
        return Response(Status(400), "Illegal State!\nCannot update miner while a miner is running.")
      else {
        val response = req.body match {
          case Some(data) => {
            try {
              val json = JSON(data.toAscii)
              AppUtils.config.updateMiner(json)
              Response(Status(200), JSON.makeJSON(Map("id" -> AppUtils.config.persistence.data.datasets(json.dataset.toString))), Seq(Header.Content.json))
            } catch {
              case je: JSONException => { Warning(data.toAscii); Response(Status(400), "Invalid JSON data.") }
              case mcpe: MinerConfigParsingException => Response(Status(400), "JSON data is not a valid miner config.")
            }
          }
          case None => Response(Status(400), "No miner data was received.")
        }
        return response
      }
    }
  }

  object DeleteNode extends NamedChildNode("delete") {
    val children = Seq()

    def execute(req: Request, segment: String): Response = {
      if (miner != null)
        return Response(Status(400), "Illegal State!\nCannot delete miner while a miner is running.")

      val id = req.query.find(p => p.name == "miner") match {
        case Some(minerId) => minerId.value.toInt
        case None => return Response(Status(400), "No miner id provided.")
      }

      val minerConfig = AppUtils.config.getMiner(id) match {
        case Some(mc) => mc
        case None => return Response(Status(400), "No miner was found with id '" + id + "'.")
      }

      AppUtils.config.deleteMiner(minerConfig.dataset)
      return Response(Status(200), "Miner deleted.")
    }
  }

  object ConfigNode extends NamedChildNode("config") {
    val children = Seq()

    def execute(req: Request, segment: String): Response = {
      val id = req.query.find(p => p.name == "miner") match {
        case Some(minerId) => minerId.value.toInt
        case None => return Response(Status(400), "No miner id provided.")
      }

      AppUtils.config("miners").find(m => {
        AppUtils.config.getMiner(id).nonEmpty && m.dataset.toString == AppUtils.config.getMiner(id).get.dataset
      }) match {
        case Some(miner) => return Response(Status(200), JSON.makeJSON(miner), Seq(Header.Content.json))
        case None => return Response(Status(400), "No miner was found with id '" + id + "'.")
      }
    }
  }

}