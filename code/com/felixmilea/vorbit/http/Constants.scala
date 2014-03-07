package com.felixmilea.vorbit.http

import akka.util.ByteString

object Constants {
  val EMPTY = ByteString.empty
  val SP = ByteString(" ")
  val TAB = ByteString("\t")
  val CRLF = ByteString("\r\n")
  val COLON = ByteString(":")
  val PERCENT = ByteString("%")
  val EQUALS = ByteString("=")
  val AMPERSAND = ByteString("&")
  val SLASH = ByteString("/")
  val QUESTION = ByteString("?")
}