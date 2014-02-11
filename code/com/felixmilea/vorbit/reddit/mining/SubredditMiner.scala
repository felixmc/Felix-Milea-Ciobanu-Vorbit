package com.felixmilea.vorbit.reddit.mining

import scala.util.parsing.json.JSON
import akka.actor.ActorRef
import com.felixmilea.vorbit.JSON.JSONParser
import com.felixmilea.vorbit.JSON.JSONTraverser
import com.felixmilea.vorbit.reddit.models.ModelParser
import com.felixmilea.vorbit.utils.Log
import com.felixmilea.vorbit.data.DataSetManager

class SubredditMiner(config: MinerConfig) extends MiningEngine(config) {

  override def mine() {
    //    for (subreddit <- config.units.par) {
    //      var after = ""
    //      for (curPage <- 0 to config.pages) {
    //        val posts = new JSONTraverser(Option(JSON.parseFull(client.get(subredditUrl(subreddit, after))).get.asInstanceOf[AnyRef]))("data")("children")
    //        val postsFound = posts(JSONParser.L).get.length
    //        Info(s"\tMining page $curPage of $subreddit/${config.postSort}: ${postsFound} threads found")
    //        for (postIndex <- (0 until postsFound).par) {
    //          val postNode = posts(postIndex)("data")
    //          if (!postNode("stickied")(JSONParser.B).get) {
    //            val post = ModelParser.parse(ModelParser.T3)(postNode)
    //            if (isValidPost(post)) {
    //              ApplicationUtils.actor("DataSetManager") ! DataSetManager.PersistPost(post, config.name)
    //              val commentJson = new JSONTraverser(Option(JSON.parseFull(client.get(commentsUrl(post.redditId))).get.asInstanceOf[AnyRef]))
    //              mineThreadComments(commentJson(1))
    //            }
    //          }
    //          if (postIndex + 1 == postsFound) after = postNode("id")().get
    //        }
    //      }
    //    }
  }

}