package com.felixmilea.vorbit.utils

import java.util.Date
import java.text.SimpleDateFormat
import java.io.FileWriter
import java.io.File

object Log {
  private val datetimeFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy")
  private val timeFormat = new SimpleDateFormat("HH:mm:ss")
  private val logFileFormat = "appdata/logs/%s.log"
  val out = System.out
  var printLevel = 0
  var fileLevel = 1
  private var logFile: String = null
  private var alternateTimestampColor = false
  private val messagePadding = 7

  def init(fLevel: Int = 1, pLevel: Int = 0) {
    printLevel = pLevel
    fileLevel = fLevel
    logFile = String.format(logFileFormat, datetime.replace(':', '.'))
    new File("appdata/logs.bob.log").createNewFile()
  }

  private def timestamp = timeFormat.format(new Date)
  private def datetime = datetimeFormat.format(new Date)

  private def writeToFile(text: String) {
    val fw = new FileWriter(logFile, true)
    fw.write(s"$text\n")
    fw.close()
  }

  protected abstract class Logger(val labelColor: String, val messageColor: String, val label: String, val level: Int) {
    val padding = " " * (7 - label.length)
    final def apply(message: String) {
      if (printLevel <= level) {
        val tColor = if (alternateTimestampColor) s"${Console.WHITE_B}${Console.BLACK}" else Console.WHITE
        out.println(s"${Console.RESET}$tColor $timestamp ${Console.RESET}$labelColor $padding$label ${Console.RESET}$messageColor $message${Console.RESET}")
        alternateTimestampColor = !alternateTimestampColor
      }
      if (fileLevel <= level)
        writeToFile(s"[$timestamp] $label:$padding $message")
    }
  }

  object Fatal extends Logger(Console.RED_B, Console.RED + Console.BOLD, "Fatal", 4)
  object Error extends Logger(Console.RED_B, Console.RED + Console.BOLD, "Error", 3)
  object Warning extends Logger(Console.YELLOW_B, Console.YELLOW + Console.BOLD, "Warning", 2)
  object Info extends Logger(Console.CYAN_B, Console.CYAN + Console.BOLD, "Info", 1)
  object Debug extends Logger(Console.GREEN_B, Console.GREEN + Console.BOLD, "Debug", 0)

}