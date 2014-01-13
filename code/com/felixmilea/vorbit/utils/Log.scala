package com.felixmilea.vorbit.utils

import java.util.Date
import java.text.SimpleDateFormat
import java.io.FileWriter
import java.io.File

object Log {
  private val datetimeFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy")
  private val timeFormat = new SimpleDateFormat("HH:mm:ss")
  private val logFileFormat = "appdata/logs/%s.log"
  private val messagePadding = 7
  private val out = System.out
  private var logFile: String = null
  var printLevel = 0
  var fileLevel = 1

  // "constructor"
  logFile = String.format(logFileFormat, datetime.replace(':', '.'))
  new File("appdata/logs.bob.log").createNewFile()
  // end

  private def timestamp = timeFormat.format(new Date)
  private def datetime = datetimeFormat.format(new Date)

  private def printlnToFile(text: String) = printToFile(s"$text\n")
  private def printToFile(text: String) {
    val fw = new FileWriter(logFile, true)
    fw.write(text)
    fw.close()
  }

  protected abstract class Logger(val labelColor: String, val messageColor: String, val label: String, val level: Int) {
    val padding = " " * (7 - label.length)
    final def apply(message: String) {
      if (printLevel <= level) {
        out.println(s"${Console.RESET}$labelColor $timestamp $padding$label ${Console.RESET}$messageColor $message${Console.RESET}")
      }
      if (fileLevel <= level)
        printlnToFile(s"[$timestamp] $label:$padding $message")
    }
  }

  object Fatal extends Logger(Console.RED_B, Console.RED + Console.BOLD, "Fatal", 4)
  object Error extends Logger(Console.RED_B, Console.RED + Console.BOLD, "Error", 3)
  object Warning extends Logger(Console.YELLOW_B, Console.YELLOW + Console.BOLD, "Warning", 2)
  object Info extends Logger(Console.CYAN_B, Console.CYAN + Console.BOLD, "Info", 1)
  object Debug extends Logger(Console.GREEN_B, Console.GREEN + Console.BOLD, "Debug", 0)

}