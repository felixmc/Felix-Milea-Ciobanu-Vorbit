package com.felixmilea.vorbit.utils

import java.util.Date

trait Loggable {
  import Loggable._

  final protected[this] lazy val log = AppUtils.log

  // optional wrapper for logged message to be used only by implement-er
  protected[this] def wrapLog(message: String): String = message

  final private def messageLog(logger: Log.Logger, logged: AnyRef) = log ! Message(logger, wrapLog(logged.toString))

  final protected[this] def Debug: LogFunction = messageLog(Log.Debug, _)
  final protected[this] def Info: LogFunction = messageLog(Log.Info, _)
  final protected[this] def Warning: LogFunction = messageLog(Log.Warning, _)
  final protected[this] def Error: LogFunction = messageLog(Log.Error, _)
  final protected[this] def Fatal: LogFunction = messageLog(Log.Fatal, _)

}

object Loggable {
  case class Message(logger: Log.Logger, text: String, timestamp: Date = new Date())
  type LogFunction = Function[AnyRef, Unit]
}