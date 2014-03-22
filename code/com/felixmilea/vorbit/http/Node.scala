package com.felixmilea.vorbit.http

import com.felixmilea.vorbit.http.Util.Request

abstract class Node {
  def children: Seq[ChildNode]

  final def execute(req: Request): Response = {
    val segment = if (req.path.length > 0) req.path.last else ""
    this.execute(req, segment)
  }
  def execute(req: Request, segment: String): Response

  def traverse(req: Request, path: PathIterator): Response = {
    // reached destination
    if (!path.hasNext) return execute(req)

    // else try pass to first child that matches
    else {
      val name = path.next
      children.find(c => {
        c.matches(name)
      }) match {
        case Some(child) => {
          return child.traverse(req, path)
        }
        case None => {
          return Response.NotFound(path.pathState)
        }
      }
    }
  }

}