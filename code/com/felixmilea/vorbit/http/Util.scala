package com.felixmilea.vorbit.http

package Util {
  import akka.util.ByteString

  trait RequestResponder {
    def respond(req: Request): Response
  }

  case class Status(code: Int, msg: String)

  object Status {
    val codes = List(
      (404 -> "Not Found"),
      (200 -> "Ok") // last
      ).map(s => {
        s._1 -> Status(s._1, s._2)
      }).toMap

    def apply(code: Int): Status = codes.getOrElse(code, Status(code, "Unknown"))
  }

  case class Parameter(name: String, value: String)
  case class Header(name: String, value: String)
  case class Request(meth: String, path: Seq[String], query: Seq[Parameter],
    httpver: String, headers: Seq[Header], body: Option[ByteString]) {
    def header(name: String): String =
      headers.find(_.name == name) match {
        case Some(header) => header.value
        case None => ""
      }
  }

}