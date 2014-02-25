package com.felixmilea.vorbit.actors

import akka.actor.ActorSelection
import com.felixmilea.vorbit.composition.NgramMarkovChain
import com.felixmilea.vorbit.composition.CommentComposer

class Composer(comp: CommentComposer) extends ManagedActor {
  import Composer._

  def doReceive = {
    case GenerateContent(receiver, n) => {
      receiver ! GeneratedContent(comp.compose())
    }
  }

}

object Composer {
  case class GenerateContent(receiver: ActorSelection, n: Int = -1)
  case class GeneratedContent(text: String)
}