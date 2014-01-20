package com.felixmilea.vorbit.utils

trait Initable {
  protected val dependencies: Seq[Initable]
  private var isInited = false

  final def isInit = isInited

  final def init() {
    dependencies.foreach(i => if (!i.isInited) throw new InitDependencyException(this, i))
    doInit()
    isInited = true
    postInit()
  }

  protected def doInit()
  protected def postInit() = {}
}