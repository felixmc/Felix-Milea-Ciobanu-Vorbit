package com.felixmilea.vorbit.reddit.mining

import akka.actor.ActorRef

class UserMiner(config: MinerConfig, entityManager: ActorRef) extends MiningEngine(config, entityManager) {

  override def mine() {

  }

}