package com.felixmilea.vorbit.utils

class InitDependencyException(val thrower: Initable, val dependency: Initable)
  extends Throwable(s"${thrower.getClass()} could not be initialized because dependency ${dependency.getClass()} has not been initialized.") {

}