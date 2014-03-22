package com.felixmilea.vorbit.http

import com.felixmilea.vorbit.http.Util.Request

abstract class RootNode extends Node {

  def respond(req: Request): Response = traverse(req, PathIterator(req.path))

}