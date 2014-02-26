package com.felixmilea.vorbit.main

import com.felixmilea.vorbit.composition.NgramManager
import com.felixmilea.vorbit.utils.Loggable
import com.felixmilea.vorbit.composition.NgramMarkovChain
import com.felixmilea.vorbit.utils.AppUtils
import com.felixmilea.vorbit.composition.CommentComposer

object CompositionTest extends App with Loggable {

  val n = 4
  val count = 10

  val (datasetId, subsetId, editionId) = (AppUtils.config.persistence.data.datasets("unidanBot"),
    AppUtils.config.persistence.data.subsets("children"), AppUtils.config.persistence.data.editions("symbolWords"))

  val composer = new CommentComposer(n, datasetId, subsetId, editionId)

  while (true) {
    (0 until count) foreach { _ =>
      Info(composer.compose())
      println
    }
    readLine()
  }

}