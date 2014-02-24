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
    var next = nextRandomState(trim(current, n))

    //    println("start: " + current)

    while (next.last != ngrams.nullId) {
      current = next
      chain += current.last
      next = nextRandomState(trim(current, n))
    }

    chain.result
  }

  private[this] def trim(list: List[Int], n: Int): List[Int] = {
    if (list.size < n) return list
    else list.drop(1)
  }

  def nextRandomState(key: List[Int]): List[Int] = {
    val set = ngrams.getSet(key.length + 1).filterKeys(k => k.startsWith(key))
    val max = set.map(kv => kv._2._1).sum
    val prob = Random.nextInt(max)
    var cumSum = 0

    val nextState = set.find(state => {
      cumSum = cumSum + state._2._1
      cumSum > prob
    }) match {
      case Some(state) => state._1
      case None => List(ngrams.nullId)
    }

    //    println("next: " + nextState + " with prob of " + ((prob + 1) / max.toDouble * 100).round + "%")

    return nextState
  }

}