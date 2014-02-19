package com.felixmilea.vorbit.reddit.mining.actors

import com.felixmilea.vorbit.reddit.mining.actors.RedditCorpusRetriever._
import com.felixmilea.vorbit.reddit.mining.SubsetAnalysisManager
import com.felixmilea.vorbit.utils.AppUtils
import com.felixmilea.vorbit.reddit.mining.actors.TextUnitProcessor.RetrieveText
import com.felixmilea.vorbit.reddit.mining.actors.TextUnitProcessor.GramSet
import com.felixmilea.vorbit.reddit.mining.actors.PostGramCache.Put

class SubsetMiningCoordinator extends ManagedActor {
  private[this] lazy val coordinator = AppUtils.actor(self.path.parent.parent.child(SubsetAnalysisManager.ActorNames.coordinator))
  private[this] lazy val downloader = AppUtils.actor(self.path.parent.parent.child(SubsetAnalysisManager.ActorNames.downloader))
  private[this] lazy val processor = AppUtils.actor(self.path.parent.parent.child(SubsetAnalysisManager.ActorNames.textProcessor))
  private[this] lazy val cache = AppUtils.actor(self.path.parent.parent.child(SubsetAnalysisManager.ActorNames.parentCache))
  private[this] lazy val joiner = AppUtils.actor(self.path.parent.parent.child(SubsetAnalysisManager.ActorNames.joiner))

  def doReceive = {
    case Post(dataset, subset, redditId, content) => {
      processor ! RetrieveText(content, dataset, subset, redditId, coordinator)
    }
    case GramSet(dataset, edition, subset, redditId, data) => {
      cache ! Put((dataset, edition, redditId), (subset, data))
      downloader ! Request(Children(dataset, subset, redditId), coordinator)
    }
    case ChildPost(dataset, subset, parent, redditId, content) => {
      processor ! RetrieveText(content, dataset, subset, parent, joiner)
    }
  }

}