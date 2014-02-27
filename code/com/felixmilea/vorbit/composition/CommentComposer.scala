package com.felixmilea.vorbit.composition

import com.felixmilea.vorbit.utils.Loggable

class CommentComposer(private[this] val ngrams: NgramManager) extends Loggable {

  def this(n: Int, dataset: Int, subset: Int, edition: Int) = {
    this(new NgramManager(n, dataset, subset, edition))
  }

  val n = ngrams.n

  private[this] val markovChain = new NgramMarkovChain(ngrams)
  private[this] val noSpaceChars = "?!.,:;"
  private[this] val whiteSpace = "[\\s]*"

  def compose(cn: Int = ngrams.n): String = {
    val sb = new StringBuilder
    val units = markovChain.generate(cn)

    for (i <- 0 until units.length) {
      sb ++= units(i).replace("NL", "  ")
      if (i + 1 < units.length && !noSpaceChars.contains(units(i + 1).head) && units(i) != "^") {
        sb += ' '
      }
    }

    val result = sb.toString.replaceAll("’", "'")

    if (result.matches(whiteSpace) || result == "deleted")
      return compose(cn)

    return result
  }

}