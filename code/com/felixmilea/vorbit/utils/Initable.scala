package com.felixmilea.vorbit.utils

trait Initable {
  protected val dependencies: Seq[Initable]
  private var isInitDone = false

  final def isInited = isInitDone

  final def init() {
    dependencies.foreach(i => if (!i.isInited) throw new InitDependencyException(this, i))
    doInit()
    isInitDone = true
    postInit()
  }

  protected def doInit()
  protected def postInit() = {}
}

class InitDependencyException(val thrower: Initable, val dependency: Initable)
  extends Throwable(s"${thrower.getClass()} could not be initialized because dependency ${dependency.getClass()} has not been initialized.")