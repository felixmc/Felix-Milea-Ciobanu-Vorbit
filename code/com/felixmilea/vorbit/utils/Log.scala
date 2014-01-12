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

  protected abstract class Logger(val color: String, val label: String, val level: Int) {
    final def apply(message: String) {
      if (printLevel <= level)
        out.println(s"[$timestamp] $color${Console.BOLD}$label:${Console.RESET}$color $message${Console.RESET}")
      if (fileLevel <= level)
        writeToFile(s"[$timestamp] $label: $message")
    }
  }

  object Fatal extends Logger(Console.RED, "Fatal", 4)
  object Error extends Logger(Console.RED, "Error", 3)
  object Warning extends Logger(Console.YELLOW, "Warning", 2)
  object Info extends Logger(Console.BLUE, "Info", 1)
  object Debug extends Logger(Console.BLACK, "Debug", 0)

}