package com.felixmilea.vorbit.reddit

import java.io.IOException
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.io.BufferedReader
import java.io.InputStreamReader

class Connection(uri: String, params: ConnectionParameters = new ConnectionParameters(), isPost: Boolean = false, headers: Map[String, String] = Map()) {
  import ConnectionUtils._

  private val data = params.toString
  private val query = if (!isPost && !data.isEmpty) s"?$data" else ""
  private val conn = URL(uri + query).openConnection().asInstanceOf[HttpURLConnection]
  private var resp: String = null

  headers.foreach(h => conn.setRequestProperty(h._1, h._2))

  conn.setRequestProperty("User-Agent", ConnectionUtils.userAgent)
  conn.setRequestProperty("charset", "utf-8")
  conn.setInstanceFollowRedirects(false)
  conn.setUseCaches(false)
  conn.setDoOutput(true)

  if (isPost) {
    conn.setRequestMethod("POST")
    conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
    conn.setRequestProperty("Content-Length", Integer.toString(data.getBytes().length))
    conn.setDoInput(true)

    try {
      writeData()
    } catch {
      case e: IOException => error("Error writing data to connection: " + e)
    }
  }

  def status = conn.getResponseCode

  private def writeData() {
    val wr = new OutputStreamWriter(conn.getOutputStream)
    wr.write(data)
    wr.flush
    wr.close
    conn.disconnect
  }

  def responseHeaders = conn.getHeaderFields
  def responseHeader(header: String) = conn.getHeaderField(header)

  def response(cached: Boolean = true): String = if (cached && resp != null) return resp else retrieveResponse
  def response: String = response(true)

  private def retrieveResponse: String = {
    val reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))

    val response = new StringBuilder
    var line: String = null

    line = reader.readLine
    while (line != null) {
      response ++= line
      line = reader.readLine
    }

    reader.close
    resp = response.toString

    return resp
  }

}