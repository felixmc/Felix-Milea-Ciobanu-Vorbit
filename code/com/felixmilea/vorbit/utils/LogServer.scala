package com.felixmilea.vorbit.utils

import java.net.Socket
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.ServerSocket

object LogServer extends Thread {
  private val CONSOLE_WIDTH = 225
  private val CONSOLE_COLOR = Console.RESET + Console.MAGENTA_B + Console.WHITE + Console.BOLD
  private val TITLE_COLOR = Console.YELLOW
  private val SPACE_BUFFER = "*"
  val PORT = 1234
  val HOSTNAME = "localhost"

  def main(args: Array[String]) {
    start()
  }

  override def run() {
    val serverSocket = new ServerSocket(PORT)
    printTitle(s"Listening for log events on port $PORT")

    while (true) {
      try {
        val socket = serverSocket.accept()
        printTitle(s"Started session with client ${socket.getInetAddress}:${socket.getPort}")
        val in = new BufferedReader(new InputStreamReader(socket.getInputStream()))
        var userInput = in.readLine()
        while (userInput != null) {
          println(userInput)
          userInput = in.readLine()
        }
      } catch {
        case t: Throwable => {
          printTitle(s"Sessions ended. Listening for log events on port $PORT")
        }
      }
    }
  }

  def printTitle(title: String) = {
    val textSize = title.length + 2
    val preText = SPACE_BUFFER * Math.round((CONSOLE_WIDTH - textSize) / 2).toInt
    val postText = SPACE_BUFFER * Math.round((CONSOLE_WIDTH - textSize) / 2).toInt
    println(s"$CONSOLE_COLOR$preText$TITLE_COLOR $title $CONSOLE_COLOR$postText")
  }
  def printBreak = println(CONSOLE_COLOR + (SPACE_BUFFER * CONSOLE_WIDTH))

}