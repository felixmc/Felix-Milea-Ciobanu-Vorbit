package com.felixmilea.vorbit.reddit.mining

abstract class MiningStrategy(private val config: MinerConfig) {

  def mine()

}