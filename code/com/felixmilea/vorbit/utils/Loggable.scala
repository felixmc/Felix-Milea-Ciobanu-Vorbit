package com.felixmilea.vorbit.utils

trait Loggable {
  private val log = ApplicationUtils.getLog
  def Debug(message: String) = log ! Loggable.Message(Log.Debug, message)
  def Info(message: String) = log ! Loggable.Message(Log.Info, message)
  def Warning(message: String) = log ! Loggable.Message(Log.Warning, message)
  def Error(message: String) = log ! Loggable.Message(Log.Error, message)
  def Fatal(message: String) = log ! Loggable.Message(Log.Fatal, message)
}

object Loggable {
  case class Message(logger: Log.Logger, text: String)
}