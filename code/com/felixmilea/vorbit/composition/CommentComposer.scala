package com.felixmilea.vorbit.composition

import com.felixmilea.vorbit.utils.Loggable

object CommentComposer extends App with Loggable {
  val dataSet = "answerBot"
  val bmc = new BigramMarkovChain(dataSet)
  val tui = new TextUnitInjector(dataSet)
  val count = 10
  val noSpaceChars = "?!.,:;"

  for (i <- 0 until count) {
    Warning("")
    Info(compose())
  }

  def compose(): String = {
    val sb = new StringBuilder
    val units = tui.parseChain(bmc.generate)

    for (i <- 0 until units.length) {
      sb ++= units(i)
      if (i + 1 < units.length && !noSpaceChars.contains(units(i + 1).head)) {
        sb += ' '
      }
    }

    sb.toString
  }

  def sep = Warning("=" * 180)

}