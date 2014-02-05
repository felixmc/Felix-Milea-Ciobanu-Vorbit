package com.felixmilea.vorbit.composition

import scala.util.Random
import scala.collection.immutable.VectorBuilder

class QuadgramMarkovChain(val dataSet: String) {
  private val bi = new BigramManager(dataSet)
  private val tri = new TrigramManager(dataSet)
  private val quad = new QuadgramManager(dataSet)
  private val TERMINATOR_ID = 1

  private val chain = new VectorBuilder[Int]

  def generate(): Seq[Int] = {
    chain.clear()

    var first = TERMINATOR_ID
    var second = bi.getNgram2(first, Random.nextInt(bi.getTotal(first)) + 1)
    var third = tri.getNgram2((first, second), Random.nextInt(tri.getTotal((first, second))) + 1)
    var fourth = quad.getNgram2((first, second, third), genFreq((first, second, third)))

    chain += second
    chain += third

    while (fourth != TERMINATOR_ID) {
      first = second
      second = third
      third = fourth

      chain += third

      fourth = quad.getNgram2((first, second, third), genFreq((first, second, third)))
    }

    return chain.result
  }

  def genFreq(id: (Int, Int, Int)): Int = Random.nextInt(quad.getTotal(id)) + 1

}