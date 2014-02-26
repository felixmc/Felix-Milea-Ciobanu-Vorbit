package com.felixmilea.vorbit.actors

import akka.actor.ActorSelection
import com.felixmilea.vorbit.composition.CommentComposer
import com.felixmilea.vorbit.actors.PostRecorder.PostingTarget
import com.felixmilea.vorbit.composition.NgramManager
import com.felixmilea.vorbit.actors.Poster.Postable

class Composer(ngrams: NgramManager) extends ManagedActor {
  import Composer._

  private[this] val comp = new CommentComposer(ngrams)

  def doReceive = {
    case GenerateContent(receiver, n) => {
      if (n < 1 || n > comp.n)
        receiver ! GeneratedContent(comp.compose())
      else
        receiver ! GeneratedContent(comp.compose(n))
    }
    case GenerateReply(target, receiver) => {
      val text = comp.compose()
      receiver ! GeneratedReply(target, text)
    }
  }

}

object Composer {
  case class GenerateContent(receiver: ActorSelection, n: Int = -1)
  case class GenerateReply(target: PostingTarget, receiver: ActorSelection)
  case class GeneratedContent(text: String)
  case class GeneratedReply(target: PostingTarget, text: String) extends Postable
}