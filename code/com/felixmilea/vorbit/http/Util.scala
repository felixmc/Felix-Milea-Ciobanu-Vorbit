package com.felixmilea.vorbit.http

package Util {
  import akka.util.ByteString

  case class Status(code: Int, msg: String)

  object Status {
    val codes = List(
      (400 -> "Bad Request"),
      (404 -> "Not Found"),
      (500 -> "Server Error"),
      (200 -> "Ok") // last
      ).map(s => {
        s._1 -> Status(s._1, s._2)
      }).toMap

    def apply(code: Int): Status = codes.getOrElse(code, Status(code, "Unknown"))
  }

  case class Header(name: String, value: String)

  object Header {
    object Content {
      val name = "Content-Type"
      val json = Header(name, "text/json")
      val plain = Header(name, "text/plain")
      val html = Header(name, "text/html")
    }
  }

  case class Parameter(name: String, value: String)

  case class Request(meth: String, path: Seq[String], query: Seq[Parameter],
    httpver: String, headers: Seq[Header], body: Option[RichByteString]) {
    def header(name: String): String =
      headers.find(_.name == name) match {
        case Some(header) => header.value
        case None => ""
      }
  }

}