package com.felixmilea.vorbit.http

import com.felixmilea.vorbit.http.Util.Request

abstract class ChildNode extends Node {
  def matches(name: String): Boolean
}

abstract class NamedChildNode(name: String) extends ChildNode {
  override def matches(segment: String): Boolean = segment == name
}

abstract class ListingNode(name: String) extends NamedChildNode(name) {
  override def traverse(req: Request, path: PathIterator): Response = {
    if (path.hasNext) {
      this.execute(req, path.next)
    } else {
      this.execute(req)
    }
  }
}

class EmbeddedChildNode(val names: Seq[String], action: Function[Request, Response], kids: Seq[ChildNode] = Seq()) extends ChildNode {
  override def children: Seq[ChildNode] = kids
  override def execute(req: Request, segment: String): Response = action(req)
  override def matches(name: String): Boolean = names.contains(name)
}