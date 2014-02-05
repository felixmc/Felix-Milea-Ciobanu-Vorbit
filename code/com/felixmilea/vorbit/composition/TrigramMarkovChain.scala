package com.felixmilea.vorbit.composition

import scala.util.Random
import scala.collection.immutable.VectorBuilder
import com.felixmilea.vorbit.utils.Loggable

class TrigramMarkovChain(val dataSet: String) extends Loggable {
  private val bi = new BigramManager(dataSet)
  private val tri = new TrigramManager(dataSet)
  private val TERMINATOR_ID = 1

  private val chain = new VectorBuilder[Int]

  def generate(): Seq[Int] = {
    chain.clear()

    var first = TERMINATOR_ID
    var second = bi.getNgram2(first, Random.nextInt(bi.getTotal(first)) + 1)
    var third = tri.getNgram2((first, second), genFreq((first, second)))

    chain += second

    while (third != TERMINATOR_ID) {
      first = second
      second = third
      chain += second
      third = tri.getNgram2((first, second), genFreq((first, second)))
    }

    return chain.result
  }

  def genFreq(id: (Int, Int)): Int = Random.nextInt(tri.getTotal(id)) + 1

}