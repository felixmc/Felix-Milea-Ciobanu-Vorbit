package com.felixmilea.vorbit.actors

import com.felixmilea.vorbit.utils.JSON

class PostingCoordinator(config: JSON) extends ManagedActor {

  def doReceive = {
    case "" => {}
  }

}