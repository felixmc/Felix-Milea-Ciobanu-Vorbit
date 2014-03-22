package com.felixmilea.vorbit.application

import com.felixmilea.vorbit.http.NamedChildNode
import com.felixmilea.vorbit.http.Response
import com.felixmilea.vorbit.http.Util.Status
import com.felixmilea.vorbit.http.Util.Request
import com.felixmilea.vorbit.composition.CommentComposer

class ComposeNode(name: String) extends NamedChildNode(name) {
  val children = Seq()

  def execute(req: Request, segment: String): Response = {

    val n = req.query.find(p => p.name == "n") match {
      case Some(nId) => nId.value.toInt
      case None => return Response(Status(400), "No n provided.")
    }

    val dataset = req.query.find(p => p.name == "dataset") match {
      case Some(datasetId) => datasetId.value.toInt
      case None => return Response(Status(400), "No dataset provided.")
    }

    val subset = req.query.find(p => p.name == "subset") match {
      case Some(subsetId) => subsetId.value.toInt
      case None => return Response(Status(400), "No subset provided.")
    }

    val edition = req.query.find(p => p.name == "edition") match {
      case Some(editionId) => editionId.value.toInt
      case None => return Response(Status(400), "No edition provided.")
    }

    val composer = new CommentComposer(n, dataset, subset, edition)
    val content = composer.compose();
    return Response(Status(200), content)
  }
}