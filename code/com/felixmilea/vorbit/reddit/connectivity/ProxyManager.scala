package com.felixmilea.vorbit.reddit.connectivity

import java.io.BufferedReader
import java.net.URL
import java.net.HttpURLConnection
import java.io.InputStreamReader
import java.net.Proxy
import java.net.InetSocketAddress
import com.felixmilea.vorbit.utils.Loggable

object ProxyManager extends Loggable {

  private val conn = new URL("http://www.socks-proxy.net/").openConnection().asInstanceOf[HttpURLConnection]
  conn.setUseCaches(false)
  conn.setDoOutput(true)

  val proxies = {
    val reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))

    val response = new StringBuilder
    var line: String = null

    line = reader.readLine
    while (line != null) {
      response ++= line + "\n"
      line = reader.readLine
    }
    reader.close

    //    (response.toString.split("<tbody>")(1).split("</tbody>")(0).lines.map(l => {
    //      val parts = l.split("(<tr><td>|</td><td>|</td></tr>)")
    //      new Proxy(Proxy.Type.SOCKS, new InetSocketAddress(parts(1), parts(2).toInt))
    //    }).toList :+ (new Proxy(Proxy.Type.SOCKS, new InetSocketAddress("localhost", 9050))))
    //      .filter(p => {
    //        try {
    //          val c = new Connection(".json")
    //          val result = c.response
    //          Info(p + " seems good.")
    //          true
    //        } catch {
    //          case t: Throwable => {
    //            Warning("Bad proxy: " + p + " due to " + t)
    //            false
    //          }
    //        }
    //      })

    List(new Proxy(Proxy.Type.SOCKS, new InetSocketAddress("localhost", 9050)))
  }

  private[this] var nextIt = 0

  def grabNext(): Proxy = this.synchronized {
    val next = proxies(nextIt)
    nextIt = (nextIt + 1) % proxies.size
    return next
  }

}