package com.felixmilea.vorbit.application

import akka.util.ByteString
import com.felixmilea.vorbit.http.Util._
import com.felixmilea.vorbit.http.Response
import com.felixmilea.vorbit.http.PathIterator
import com.felixmilea.vorbit.http.RootNode
import com.felixmilea.vorbit.http.NamedChildNode
import com.felixmilea.vorbit.http.ChildNode
import com.felixmilea.vorbit.utils.JSON
import com.felixmilea.vorbit.utils.AppUtils

class ApplicationRoot extends RootNode {
  val children = Seq(new MinerNodeManager("miners"), new PosterNodeManager("posters"),
    new DataListingNode("subsets", AppUtils.config.persistence.data.subsets),
    new DataListingNode("editions", AppUtils.config.persistence.data.editions), new ComposeNode("compose"))

  def execute(req: Request, segment: String): Response = {
    Response(Status(200), "welcome to vorbit!!")
  }

}

class DataListingNode(name: String, defdata: Map[String, Int] = Map(), kids: Seq[ChildNode] = Seq()) extends NamedChildNode(name) {
  override def children: Seq[ChildNode] = kids
  protected[this] def data: Map[String, Int] = defdata

  def execute(req: Request, segment: String): Response = {
    val formattedData = data.map(d => Map[String, Any](("name" -> d._1), ("id" -> d._2)))
    val json = JSON.makeJSON(formattedData)
    Response(Status(200), json, Seq(Header.Content.json))
  }

}