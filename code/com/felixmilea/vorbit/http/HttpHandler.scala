package com.felixmilea.vorbit.http

import scala.collection.mutable.ListBuffer
import akka.actor.Actor
import akka.io.{ IO, Tcp }
import akka.util.ByteString
import com.felixmilea.vorbit.http.Util._
import com.felixmilea.vorbit.utils.Loggable

class HttpHandler(responder: RootNode) extends Actor with Loggable {
  import Tcp._
  import com.felixmilea.vorbit.http.Constants._
  import com.felixmilea.vorbit.http.RichByteString._

  def receive = {
    case Received(data) => {
      val req = readRequest(data)
      Debug("req:  " + req)

      val resp = responder.respond(req)
      Warning("resp: " + resp)

      sender() ! Write(resp.compose)

      if (req.header("connection") != "keep-alive") {
        sender() ! ConfirmedClose
      }

    }
    case PeerClosed => context stop self
  }

  def readRequest(data: RichByteString): Request = {
    val (meth, path, query, httpver) = readRequestLine(data)
    val headers = readHeaders(data)
    return Request(meth, path, query, httpver, headers, if (data.isEmpty) None else Some(data))
  }

  def readRequestLine(data: RichByteString): (String, Seq[String], Seq[Parameter], String) = {
    val meth = data takeUntil SP
    val (path, query) = readRequestURI(data)
    val httpver = data takeUntil CRLF
    return (meth.toAscii, path, query, httpver.toAscii)
  }

  def readRequestURI(data: RichByteString): (Seq[String], Seq[Parameter]) = {
    val uri: RichByteString = (data takeUntil SP)
    val path = parsePath(uri takeUntil QUESTION)
    val query = parseQuery(uri)

    (path, query)
  }

  def parsePath(data: RichByteString): Seq[String] = {
    val builder = new ListBuffer[String]
    while (!data.isEmpty) {
      val segment = data takeUntil SLASH
      if (!segment.isEmpty) builder += segment.toAscii
    }

    return builder.toSeq
  }

  def parseQuery(data: RichByteString): Seq[Parameter] = {
    val builder = new ListBuffer[Parameter]
    while (!data.isEmpty) {
      val name = data takeUntil EQUALS
      val value = data takeUntil AMPERSAND
      if (!name.isEmpty && !value.isEmpty) builder += Parameter(name.toAscii, value.toAscii)
    }

    return builder.toSeq
  }

  def readHeaders(data: RichByteString): Seq[Header] = {
    var header: RichByteString = data takeUntil CRLF
    val builder = new ListBuffer[Header]

    while (!header.isEmpty) {
      builder += Header((header takeUntil COLON).toAscii.toLowerCase, header.toAscii.toLowerCase)
      header = data takeUntil CRLF
    }

    return builder.toSeq
  }

}