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
    var fourth = nextRandomState((first, second, third))

    chain += second
    chain += third

    while (fourth != TERMINATOR_ID) {
      first = second
      second = third
      third = fourth

      chain += third

      fourth = nextRandomState((first, second, third))
    }

    return chain.result
  }

  def genFreq(id: (Int, Int, Int)): Int = Random.nextInt(quad.getTotal(id)) + 1

  def nextRandomState(id: (Int, Int, Int)): Int = {
    def randProb = Random.nextInt(quad.getTotal(id))
    def randChoice = Random.shuffle(quad.getEndStates(id)).head
    var nextState = -1

    while (nextState == -1) {
      val prob = randProb
      val choice = randChoice
      if (quad.getFreq(id)(choice) > randProb) {
        nextState = choice
      }
    }

    return nextState
  }

}