package com.felixmilea.vorbit.reddit.mining

import akka.actor.Props
import akka.routing.SmallestMailboxRouter
import com.felixmilea.vorbit.actors.TextUnitProcessor
import com.felixmilea.vorbit.actors.RedditCorpusRetriever
import com.felixmilea.vorbit.actors.SubsetNgramJoiner
import com.felixmilea.vorbit.actors.SubsetMiningCoordinator
import com.felixmilea.vorbit.actors.PostGramCache
import com.felixmilea.vorbit.actors.SubsetNgramAnalyzer
import com.felixmilea.vorbit.actors.ActorSetManager

class SubsetAnalysisManager extends ActorSetManager {
  import SubsetAnalysisManager._

  val name = "SubsetAnalysisManager"
  val managedActors = Map(
    (ActorNames.downloader -> Props[RedditCorpusRetriever].withRouter(SmallestMailboxRouter(10))),
    (ActorNames.coordinator -> Props[SubsetMiningCoordinator].withRouter(SmallestMailboxRouter(20))),
    (ActorNames.textProcessor -> Props[TextUnitProcessor].withRouter(SmallestMailboxRouter(15))),
    (ActorNames.parentCache -> Props[PostGramCache]),
    (ActorNames.joiner -> Props[SubsetNgramJoiner].withRouter(SmallestMailboxRouter(30))),
    (ActorNames.analyzer -> Props[SubsetNgramAnalyzer].withRouter(SmallestMailboxRouter(20))) // last
    )

  init()
}

object SubsetAnalysisManager {
  object ActorNames {
    val downloader = "downloader"
    val coordinator = "coordinator"
    val textProcessor = "textProcessor"
    val parentCache = "parentCache"
    val joiner = "joiner"
    val analyzer = "analyzer"
  }
}