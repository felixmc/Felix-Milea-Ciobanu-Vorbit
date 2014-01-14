package com.felixmilea.vorbit.utils

import java.net.Socket
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.ServerSocket

object LogServer extends Thread {
  val PORT = 1234
  val HOSTNAME = "localhost"

  def main(args: Array[String]) {
    start()
  }

  override def run() {
    val serverSocket = new ServerSocket(PORT)
    while (true) {
      try {
        val socket = serverSocket.accept()
        val in = new BufferedReader(new InputStreamReader(socket.getInputStream()))
        var userInput = in.readLine()
        while (userInput != null) {
          println(userInput)
          userInput = in.readLine()
        }
      } catch {
        case t: Throwable => println(Console.MAGENTA_B + Console.WHITE + ("=" * 225))
      }
    }
  }

}