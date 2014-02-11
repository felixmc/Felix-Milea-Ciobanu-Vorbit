package com.felixmilea.vorbit.utils

import java.util.Date

trait Loggable {
  protected[this] lazy val log = App.log
  def Debug(message: String) = log ! Loggable.Message(Log.Debug, message)
  def Info(message: String) = log ! Loggable.Message(Log.Info, message)
  def Warning(message: String) = log ! Loggable.Message(Log.Warning, message)
  def Error(message: String) = log ! Loggable.Message(Log.Error, message)
  def Fatal(message: String) = log ! Loggable.Message(Log.Fatal, message)
}

object Loggable {
  case class Message(logger: Log.Logger, text: String, timestamp: Date = new Date())
}