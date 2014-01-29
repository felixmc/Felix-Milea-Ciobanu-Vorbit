package com.felixmilea.vorbit.utils

import java.io.PrintStream
import java.io.FileWriter
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import akka.actor.Actor

class Log(val printLevel: Int = 0, val fileLevel: Int = 1, val out: PrintStream = System.out) extends Actor {
  private val datetimeFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy")
  private val timeFormat = new SimpleDateFormat("HH:mm:ss")
  private val logFile: String = String.format(Log.logFileFormat, datetime.replace(':', '.'))

  // create log file
  new File("appdata/logs.bob.log").createNewFile()

  def this() = this(0, 1, System.out)

  private def timestamp = timeFormat.format(new Date)
  private def datetime = datetimeFormat.format(new Date)

  private def printlnToFile(text: String) = printToFile(s"$text\n")
  private def printToFile(text: String) {
    val fw = new FileWriter(logFile, true)
    fw.write(text)
    fw.close()
  }

  def receive = {
    case Loggable.Message(logger, message) => {
      val padding = " " * (Log.messagePadding - logger.label.length)
      if (printLevel <= logger.level)
        out.println(s"${Console.RESET}${logger.labelColor} $timestamp $padding${logger.label} ${Console.RESET}${logger.messageColor} ${message}${Console.RESET}")
      if (fileLevel <= logger.level)
        printlnToFile(s"[$timestamp] ${logger.label}:$padding ${message}")
    }
  }

}

object Log {
  var logFileFormat = "appdata/logs/%s.log"
  var messagePadding = 7

  abstract class Logger(val labelColor: String, val messageColor: String, val label: String, val level: Int)
  object Fatal extends Logger(Console.RED_B, Console.RED + Console.BOLD, "Fatal", 4)
  object Error extends Logger(Console.RED_B, Console.RED + Console.BOLD, "Error", 3)
  object Warning extends Logger(Console.YELLOW_B, Console.YELLOW + Console.BOLD, "Warning", 2)
  object Info extends Logger(Console.CYAN_B, Console.CYAN + Console.BOLD, "Info", 1)
  object Debug extends Logger(Console.GREEN_B, Console.GREEN + Console.BOLD, "Debug", 0)
}