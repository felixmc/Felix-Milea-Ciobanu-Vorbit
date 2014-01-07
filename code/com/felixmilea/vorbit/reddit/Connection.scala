package com.felixmilea.vorbit.reddit

import java.io.IOException
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.io.BufferedReader
import java.io.InputStreamReader

class Connection(uri: String, params: ConnectionParameters = new ConnectionParameters) {
  import ConnectionUtils._

  private val conn = URL(uri).openConnection().asInstanceOf[HttpURLConnection]
  private val data = params.toString
  private var resp: String = null

  conn.setDoOutput(true)
  conn.setDoInput(true)
  conn.setInstanceFollowRedirects(false)
  conn.setRequestMethod("POST")
  conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
  conn.setRequestProperty("charset", "utf-8")
  conn.setRequestProperty("User-Agent", ConnectionUtils.userAgent)
  conn.setRequestProperty("Content-Length", Integer.toString(data.getBytes().length))
  conn.setUseCaches(false)

  try {
    writeData()
  } catch {
    case e: IOException => error("Error writing data to connection: " + e)
  }

  def status = conn.getResponseCode

  private def writeData() {
    val wr = new OutputStreamWriter(conn.getOutputStream)
    wr.write(data)
    wr.flush
    wr.close
    conn.disconnect
  }

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