package com.felixmilea.vorbit.main

import com.felixmilea.vorbit.composition.NgramManager
import com.felixmilea.vorbit.utils.Loggable
import com.felixmilea.vorbit.composition.NgramMarkovChain
import com.felixmilea.vorbit.utils.AppUtils

object CompositionTest extends App with Loggable {

  val n = 1
  val count = 10

  val (datasetId, subsetId, editionId) = (AppUtils.config.persistence.data.datasets("maybeHumanBot"),
    AppUtils.config.persistence.data.subsets("children"), AppUtils.config.persistence.data.editions("symbolWords"))

  val ngrams = new NgramManager(n, datasetId, subsetId, editionId)
  val chain = new NgramMarkovChain(ngrams)

  while (true) {
    (0 until count) foreach { _ =>
      Info(chain.generate(n).mkString(" "))
      Info("")
    }
    readLine()
  }

}