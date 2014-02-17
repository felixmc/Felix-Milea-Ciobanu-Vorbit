package com.felixmilea.vorbit.utils

import scala.language.dynamics

trait MappedProps[T] extends Dynamic {
  protected[this] val propMap: Map[String, T]
  def selectDynamic(name: String): T = propMap(name)
}