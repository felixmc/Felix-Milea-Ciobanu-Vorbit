package com.felixmilea.vorbit.reddit.mining

import com.felixmilea.vorbit.reddit.mining.config.MinerConfig
import com.felixmilea.vorbit.utils.Loggable

abstract class Miner() extends Thread with Loggable {
  protected[this] val manager: MiningManager
}