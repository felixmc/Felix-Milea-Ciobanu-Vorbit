package com.felixmilea.vorbit.composition

import scala.util.Random
import scala.collection.immutable.VectorBuilder

class BigramMarkovChain(val dataSet: String) {
  private val bi = new BigramManager(dataSet)
  private val TERMINATOR_ID = 1

  private val chain = new VectorBuilder[Int]

  def generate(): Seq[Int] = {
    chain.clear()

    var current = TERMINATOR_ID
    var nextId = bi.getNgram2(current, genFreq(current))

    while (nextId != TERMINATOR_ID) {
      current = nextId
      chain += current
      nextId = bi.getNgram2(current, genFreq(current))
    }

    return chain.result
  }

  def genFreq(id: Int): Int = Random.nextInt(bi.getTotal(id)) + 1

}