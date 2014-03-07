package com.felixmilea.vorbit.http

import akka.util.ByteString
import com.felixmilea.vorbit.http.Util._

class ApplicationLogicResponder extends RequestResponder {

  def respond(req: Request): Response = {
    val resp = Response(Status(200), ByteString("hello there"), List(Header("Content-Type", "text/plain")))
    return resp
  }

}