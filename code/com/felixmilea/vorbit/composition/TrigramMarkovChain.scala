package com.felixmilea.vorbit.composition

import scala.util.Random
import scala.collection.immutable.VectorBuilder
import com.felixmilea.vorbit.utils.Loggable

class TrigramMarkovChain(val dataSet: String) {
  private val bi = new BigramManager(dataSet)
  private val tri = new TrigramManager(dataSet)
  private val TERMINATOR_ID = 1

  private val chain = new VectorBuilder[Int]

  def generate(): Seq[Int] = {
    chain.clear()

    var first = TERMINATOR_ID
    var second = bi.getNgram2(first, Random.nextInt(bi.getTotal(first)) + 1)
    var third = nextRandomState((first, second))

    chain += second

    while (third != TERMINATOR_ID) {
      first = second
      second = third
      chain += second
      third = nextRandomState((first, second))
    }

    return chain.result
  }

  def genFreq(id: (Int, Int)): Int = Random.nextInt(tri.getTotal(id)) + 1

  def nextRandomState(id: (Int, Int)): Int = {
    def randProb = Random.nextInt(tri.getTotal(id))
    def randChoice = Random.shuffle(tri.getEndStates(id)).head
    var nextState = -1

    while (nextState == -1) {
      val prob = randProb
      val choice = randChoice
      if (tri.getFreq(id)(choice) > randProb) {
        nextState = choice
      }
    }

    return nextState
  }

}