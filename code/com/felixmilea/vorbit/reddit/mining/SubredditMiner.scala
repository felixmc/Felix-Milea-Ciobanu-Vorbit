package com.felixmilea.vorbit.reddit.mining

import com.felixmilea.vorbit.utils.Log
import com.felixmilea.vorbit.JSON.JSONTraverser
import com.felixmilea.vorbit.reddit.models.ModelParser
import scala.util.parsing.json.JSON
import com.felixmilea.vorbit.data.EntityManager
import com.felixmilea.vorbit.JSON.JSONParser
import com.felixmilea.vorbit.reddit.models.RedditPostParseException
import com.felixmilea.vorbit.analysis.WordParser

class SubredditMiner(config: MinerConfig) extends MiningEngine(config) {

  override def mine() {
    for (subreddit <- config.units.par) {
      val posts = new JSONTraverser(Option(JSON.parseFull(client.get(subredditUrl(subreddit))).get.asInstanceOf[AnyRef]))("data")("children")
      Log.Info(s"\tMining subreddit `$subreddit`: ${posts(JSONParser.L).get.length} threads found")
      for (postIndex <- (0 until posts(JSONParser.L).get.length).par) {
        val postNode = posts(postIndex)("data")
        val post = ModelParser.parse(ModelParser.T3)(postNode)
        if (isValidPost(post)) {
          if (EntityManager.insertPost(post, config.name))
            Log.Debug(s"\t\tMining post `${post.redditId}` ngrams found: ${WordParser.parseAsText(post.title)}")

          val commentJson = new JSONTraverser(Option(JSON.parseFull(client.get(commentsUrl(post.redditId))).get.asInstanceOf[AnyRef]))
          val commentNode = commentJson(1)("data")("children")

          for (comIndex <- 0 until commentNode(JSONParser.L).get.length) {
            if (commentNode(comIndex)("kind")().get == "t1") {
              val comment = ModelParser.parse(ModelParser.T1)(commentNode(comIndex)("data"))
              if (isValidComment(comment)) {
                if (EntityManager.insertPost(comment, config.name))
                  Log.Debug(s"\t\tMinining comment `${comment.redditId}` on thread `${post.redditId}` ngrams found: ${WordParser.parseAsText(comment.content)}")
              }
            }
          }
        }
      }
    }
  }

}