package com.felixmilea.vorbit.reddit.mining

import com.felixmilea.vorbit.reddit.connectivity.Client
import scala.util.parsing.json.JSON
import com.felixmilea.vorbit.JSON.JSONParser
import com.felixmilea.vorbit.JSON.JSONTraverser
import com.felixmilea.vorbit.reddit.models.ModelParser
import com.felixmilea.vorbit.reddit.models.RedditPostParseException
import com.felixmilea.vorbit.reddit.models.Comment
import com.felixmilea.vorbit.reddit.models.Post
import com.felixmilea.vorbit.data.EntityManager
import com.felixmilea.vorbit.analysis.WordParser
import com.felixmilea.vorbit.utils.Log
import java.util.Date

class Miner(private val config: MinerConfig) extends Thread {
  private val DELAY = (1000 * 60) * 5
  private val engine = MiningEngine.get(config)

  EntityManager.setupMiner(config.name)

  override def run() {
    while (true) {
      Log.Info(s"Starting data mining operation `${config.name}`")
      engine.mine
      Log.Info(s"Pausing data mining operation `${config.name}` for $DELAY ms")
      Thread.sleep(DELAY)
    }
  }

  //  override def run() {
  //    EntityManager.setupMiner(config.name)
  //    Log.Info(s"Starting data mining operation `${config.name}`")
  //
  //    for (subreddit <- /*config.subreddits*/ List("askReddit")) {
  //      val posts = new JSONTraverser(Option(JSON.parseFull(client.get(s"r/$subreddit/hot.json?limit=100")).get.asInstanceOf[AnyRef]))("data")("children")
  //      Log.Info(s"\tMining subreddit `$subreddit`: ${posts(JSONParser.L).get.length} threads found")
  //      for (postIndex <- 0 until posts(JSONParser.L).get.length) {
  //        val postNode = posts(postIndex)("data")
  //        val post = ModelParser.parse(ModelParser.T3)(postNode)
  //        if (isValidPost(post)) {
  //          if (EntityManager.insertPost(post, config.name))
  //            Log.Debug(s"\t\tMining post `${post.redditId}` ngrams found: ${WordParser.parseAsText(post.title)}")
  //
  //          val commentJson = new JSONTraverser(Option(JSON.parseFull(client.get(s"comments/${post.redditId}.json?sort=top")).get.asInstanceOf[AnyRef]))
  //          val commentNode = commentJson(1)("data")("children")
  //
  //          for (comIndex <- 0 until commentNode(JSONParser.L).get.length) {
  //            if (commentNode(comIndex)("kind")().get == "t1")
  //              try {
  //                val comment = ModelParser.parse(ModelParser.T1)(commentNode(comIndex)("data"))
  //                if (isValidComment(comment)) {
  //                  if (EntityManager.insertPost(comment, config.name))
  //                    Log.Debug(s"\t\tMinining comment `${comment.redditId}` on thread `${post.redditId}` ngrams found: ${WordParser.parseAsText(comment.content)}")
  //                }
  //              } catch {
  //                case rppe: RedditPostParseException => {
  //                  val id = commentNode(comIndex)("data")("id")().get
  //                  Log.Error(s"\t\tError parsing comment `$id` on thread `${post.redditId}`: `${rppe.json.data}`")
  //                }
  //              }
  //          }
  //        }
  //      }
  //    }

  //    Log.Info(s"Commencing data mining operation `${config.name}`")
  //  }

}