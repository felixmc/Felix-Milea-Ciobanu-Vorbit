package com.felixmilea.vorbit.http

import akka.util.ByteString
import akka.util.ByteStringBuilder
import com.felixmilea.vorbit.http.Util._

case class Response(status: Status, body: ByteString, headers: Seq[Header] = Response.defaultHeaders) {
  import Response._
  import Constants._

  lazy val compose: ByteString = {
    val builder = new ByteStringBuilder

    // status line
    builder ++= httpver ++ SP ++ status.code.toString ++ SP ++ status.msg ++ CRLF

    var hasContentLength = false

    // headers
    headers.foreach(h => {
      hasContentLength = lengthHeader == h.name.toLowerCase
      builder ++= EMPTY ++ h.name ++ COLON ++ SP ++ h.value ++ CRLF
    })

    // add content length header if missing
    if (!hasContentLength) {
      builder ++= EMPTY ++ lengthHeader ++ COLON ++ SP ++ body.length.toString ++ CRLF
    }

    // body
    builder ++= CRLF ++ body

    builder.result
  }
}

object Response {
  val httpver = ByteString("HTTP/1.1")
  val defaultHeaders = Seq(Header("Content-Type", "text/plain"))
  val lengthHeader = "content-length"

  def apply(status: Status, sbody: String, headers: Seq[Header]): Response = Response(status, ByteString(sbody), headers)
  def apply(status: Status, sbody: String): Response = Response(status, ByteString(sbody), defaultHeaders)

  implicit def stringToBytes(s: String): ByteString = ByteString(s)
}