package com.felixmilea.vorbit.main

import com.felixmilea.vorbit.composition.NgramManager
import com.felixmilea.vorbit.utils.Loggable
import com.felixmilea.vorbit.composition.NgramMarkovChain

object CompositionTest extends App with Loggable {

  val n = 3
  val count = 10

  val ngrams = new NgramManager(n, "answerBot", "children", "symbolWords")
  val chain = new NgramMarkovChain(ngrams)

  while (true) {
    (0 until count) foreach { _ =>
      Info(chain.generate(n).mkString(" "))
      Info("")
    }
    readLine()
  }

}