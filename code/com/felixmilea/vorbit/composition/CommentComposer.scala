package com.felixmilea.vorbit.composition

import com.felixmilea.vorbit.utils.Loggable

class CommentComposer(n: Int, dataset: Int, subset: Int, edition: Int) extends Loggable {
  val ngrams = new NgramManager(n, dataset, subset, edition)
  val markovChain = new NgramMarkovChain(ngrams)
  val noSpaceChars = "?!.,:;"

  def compose(): String = {
    val sb = new StringBuilder
    val units = markovChain.generate(n)

    for (i <- 0 until units.length) {
      sb ++= units(i).replace("NL", "  ")
      if (i + 1 < units.length && !noSpaceChars.contains(units(i + 1).head)) {
        sb += ' '
      }
    }

    sb.toString
  }

}