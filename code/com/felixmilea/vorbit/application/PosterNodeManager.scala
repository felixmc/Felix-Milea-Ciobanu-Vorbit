package com.felixmilea.vorbit.application

import com.felixmilea.vorbit.http.ChildNode
import com.felixmilea.vorbit.http.Response
import com.felixmilea.vorbit.http.Util.Status
import com.felixmilea.vorbit.http.Util.Request
import com.felixmilea.vorbit.http.NamedChildNode
import com.felixmilea.vorbit.utils.AppUtils
import com.felixmilea.vorbit.utils.Loggable
import akka.actor.Props
import com.felixmilea.vorbit.posting.PostingManager
import com.felixmilea.vorbit.posting.RedditPoster
import com.felixmilea.vorbit.utils.JSON
import com.felixmilea.vorbit.utils.JSONException
import com.felixmilea.vorbit.http.Util.Header

class PosterNodeManager(name: String) extends DataListingNode(name) {
  import PosterNodeManager._

  private[this] val kids = Seq(StartNode, StopNode, StatusNode, UpdateNode, DeleteNode, ConfigNode)
  override def children: Seq[ChildNode] = kids

  override protected[this] def data: Map[String, Int] = {
    var id = -1
    AppUtils.config("posters")
      .filter(p => p.active)
      .map(p => {
        val name: String = p.name
        id += 1
        name -> id
      }).toMap
  }

}

object PosterNodeManager extends Loggable {
  val managerName = "PostingManager"
  lazy val manager = AppUtils.actorSystem.actorOf(Props(new PostingManager(1)), managerName)

  var poster: RedditPoster = null

  object StartNode extends NamedChildNode("start") {
    val children = Seq()

    def execute(req: Request, segment: String): Response = {
      if (poster != null) return Response(Status(400), "A poster is already running.")

      val id = req.query.find(p => p.name == "poster") match {
        case Some(posterId) => posterId.value
        case None => return Response(Status(400), "No poster id provided.")
      }

      AppUtils.config.getPoster(id) match {
        case None => return Response(Status(400), "No poster was found with id '" + id + "'.")
        case Some(posterConfig) => try {
          poster = new RedditPoster(posterConfig, manager)
          poster.start()
          return Response(Status(200), "Poster started.")
        } catch {
          case t: Throwable => {
            Error(s"Unexpected error occured while creating or running poster #${posterConfig.name}: ${t.getMessage}")
            return Response(Status(500), "Error starting poster.")
          }
        }
      }
    }
  }

  object StopNode extends NamedChildNode("stop") {
    val children = Seq()

    def execute(req: Request, segment: String): Response = {
      if (poster == null) return Response(Status(500), "No poster currently running.")

      poster.getStatus match {
        case 0 => poster.gentleStop
        case 1 => poster.forceStop
      }

      poster = null
      return Response(Status(200), "Poster stopped.")
    }
  }

  object StatusNode extends NamedChildNode("status") {
    val children = Seq()

    def execute(req: Request, segment: String): Response = {
      if (poster == null)
        return Response(Status(200), "{'active': false}", Seq(Header.Content.json))
      else
        return Response(Status(200), "{'active': true, 'poster': " + poster.name + ", 'status': " + poster.getStatus + "}", Seq(Header.Content.json))
    }
  }

  object UpdateNode extends NamedChildNode("update") {
    val children = Seq()

    def execute(req: Request, segment: String): Response = {
      if (poster != null)
        return Response(Status(400), "Illegal State!\nCannot update poster while a poster is running.")
      else {
        req.body match {
          case Some(data) => {
            try {
              val json = JSON(data.toAscii)
              AppUtils.config.updatePoster(json)
              return Response(Status(200), "Poster updated successfully.")
            } catch {
              case je: JSONException => Response(Status(400), "Invalid JSON data.")
            }
          }
          case None => Response(Status(400), "No poster data was received.")
        }
      }
    }
  }

  object DeleteNode extends NamedChildNode("delete") {
    val children = Seq()

    def execute(req: Request, segment: String): Response = {
      if (poster != null)
        return Response(Status(400), "Illegal State!\nCannot delete poster while a poster is running.")

      val id = req.query.find(p => p.name == "poster") match {
        case Some(posterId) => posterId.value
        case None => return Response(Status(400), "No poster id provided.")
      }

      val config = AppUtils.config.getPoster(id) match {
        case Some(pc) => pc
        case None => return Response(Status(400), "No poster was found with id '" + id + "'.")
      }

      AppUtils.config.deletePoster(config.name)
      return Response(Status(200), "Poster deleted.")
    }
  }

  object ConfigNode extends NamedChildNode("config") {
    val children = Seq()

    def execute(req: Request, segment: String): Response = {
      val id = req.query.find(p => p.name == "poster") match {
        case Some(posterId) => posterId.value
        case None => return Response(Status(400), "No poster id provided.")
      }

      AppUtils.config.getPoster(id) match {
        case Some(poster) => return Response(Status(200), JSON.makeJSON(poster), Seq(Header.Content.json))
        case None => return Response(Status(400), "No poster was found with id '" + id + "'.")
      }
    }
  }
}