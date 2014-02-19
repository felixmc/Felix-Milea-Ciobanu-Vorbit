package com.felixmilea.vorbit.reddit.mining.actors

import scala.collection.mutable.HashMap
import com.felixmilea.vorbit.data.DBConnection
import com.felixmilea.vorbit.utils.AppUtils
import com.felixmilea.vorbit.reddit.mining.SubsetAnalysisManager
import com.felixmilea.vorbit.reddit.mining.actors.PostGramCache._
import com.felixmilea.vorbit.reddit.mining.actors.TextUnitProcessor.GramSet
import com.felixmilea.vorbit.reddit.mining.actors.SubsetNgramAnalyzer.ParentChild

class SubsetNgramJoiner extends ManagedActor {
  import SubsetNgramJoiner._

  private[this] lazy val parentCache = AppUtils.actor(self.path.parent.parent.child(SubsetAnalysisManager.ActorNames.parentCache))
  private[this] lazy val joiner = AppUtils.actor(self.path.parent.parent.child(SubsetAnalysisManager.ActorNames.joiner))
  private[this] lazy val analyzer = AppUtils.actor(self.path.parent.parent.child(SubsetAnalysisManager.ActorNames.analyzer))

  private[this] val delay = 1000
  private[this] val maxTries = 3
  private[this] var tries = 0
  private[this] var tempCache: Entry = null

  def doReceive = {
    // if received child grams
    case GramSet(dataset, edition, subset, id, data) => {
      // if local cache is empty (not waiting for an entry)
      if (tempCache == null) {
        // store in local cache
        tempCache = Entry((dataset, edition, id), Some((subset, data)))

        // request parent from parent cache
        requestParent()
      } //
      // else forward to a sibling
      else {
        joiner ! GramSet(dataset, edition, subset, id, data)
      }
    }
    case Entry(key, valueOption) =>
      // if temp cache isn't null and entry has the right key
      if (tempCache != null && key == tempCache.key) {

        valueOption match {
          case Some(value) => {
            analyzer ! ParentChild(key._1, key._2, value, tempCache.value.get)
            reset()
          }
          // if not post was received
          case None => {
            tries = tries + 1
            if (tries < maxTries) {
              Error("Bad cache retrieval: post not found: " + tempCache.key + s"\t attempt #${tries}\tattempting again in ${delay * tries}ms")
              Thread.sleep(delay * tries)
              requestParent()
            } //
            // if retry attempts maxed out
            else {
              Error("Bad cache retrieval: post not found: " + tempCache.key + s"\t giving up after ${tries} attempts!")
              reset()
            }
          }
        }
      } //
      // else if cache is null
      else if (tempCache == null) {
        Error("Bad cache retrieval: unexpected post received: " + key)
      } //
      // else if the wrong entry was received
      else {
        Error("Bad cache retrieval: expecting post: " + tempCache.key + "\t but received post: " + key)
      }
  }

  private[this] def requestParent() {
    if (tempCache == null) {
      Error("Parent request attempted with no local cache")
    } else {
      parentCache ! Get(tempCache.key)
    }
  }

  private[this] def reset() {
    tempCache = null
    tries = 0
  }

}

object SubsetNgramJoiner {
  val minLevel = 2
  val maxLevel = 4
}