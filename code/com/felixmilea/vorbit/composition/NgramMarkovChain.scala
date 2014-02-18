package com.felixmilea.vorbit.composition

import scala.collection.immutable.VectorBuilder
import scala.util.Random

class NgramMarkovChain(ngrams: NgramManager) {
  private[this] val chain = new VectorBuilder[Int]

  def generate(n: Int = ngrams.n): Seq[String] = generateRaw(n).map(id => ngrams.textUnits(id))

  def generateRaw(n: Int = ngrams.n): Seq[Int] = {
    if (n < 1 || n > ngrams.n) throw new IllegalArgumentException(s"Value '$n' for parameter n does not satisfy n > 0 && n <= ${ngrams.n}")
    chain.clear

    var current = List(ngrams.nullId)
    chain += current.last
    var next = nextRandomState(trim(current, n))

    while (next.last != ngrams.nullId) {
      current = next
      chain += current.last
      next = nextRandomState(trim(current, n))
    }

    chain += next.last
    chain.result
  }

  private[this] def trim(list: List[Int], n: Int): List[Int] = {
    if (list.size < n) return list
    else list.drop(1)
  }

  def nextRandomState(key: List[Int]): List[Int] = {
    val set = ngrams.getSet(key.length + 1).filterKeys(k => k.startsWith(key))
    val keys = set.keys.toList
    val count = set.size
    val max = set.map(kv => kv._2).sum

    var nextState: List[Int] = List()

    while (nextState.isEmpty) {
      val prob = Random.nextInt(max)
      val choice = Random.shuffle(keys).head
      if (set(choice) > prob) {
        nextState = choice
      }
    }

    return nextState
  }

}