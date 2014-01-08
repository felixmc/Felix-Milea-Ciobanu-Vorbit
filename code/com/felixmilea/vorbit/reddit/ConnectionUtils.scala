package com.felixmilea.vorbit.reddit

import java.net.URL
import java.util.HashMap

object ConnectionUtils {
  val version = "0.1"

  val RN = "\r\n"

  val baseURL = "http://www.reddit.com/"
  val authURL = new URL(baseURL + "login")

  val userAgent = s"VorbitBot/${version} (http://felixmilea.com/vorbitbot/)"

  def URL(path: String) = new URL(baseURL + path)
}