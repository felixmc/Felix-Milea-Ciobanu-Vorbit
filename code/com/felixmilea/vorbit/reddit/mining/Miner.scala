package com.felixmilea.vorbit.reddit.mining

import com.felixmilea.vorbit.reddit.mining.config.MinerConfig
import com.felixmilea.vorbit.utils.Loggable
import com.felixmilea.vorbit.actors.ActorSetManager

abstract class Miner() extends Thread with Loggable {
  protected[this] val manager: ActorSetManager
}