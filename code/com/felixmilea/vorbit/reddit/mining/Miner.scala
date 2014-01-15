package com.felixmilea.vorbit.reddit.mining

import com.felixmilea.vorbit.reddit.connectivity.Credential
import com.felixmilea.vorbit.reddit.connectivity.Client
import com.felixmilea.vorbit.JSON.JSONTraverser
import com.felixmilea.vorbit.reddit.models.ModelParser
import com.felixmilea.vorbit.data.EntityManager
import scala.util.parsing.json.JSON
import com.felixmilea.vorbit.JSON.JSONParser
import com.felixmilea.vorbit.utils.Log
import java.util.Date

class Miner(val config: MinerConfig, posts: Int) extends Thread {

  private var postCount = 0

  val token = Credential.fromFile("config/miners.config").head
  val client = new Client
  client.authenticate(token)

  override def run() {

    for (subreddit <- config.subreddits) {
      val posts = new JSONTraverser(Option(JSON.parseFull(client.get(s"r/$subreddit/hot.json")).get.asInstanceOf[AnyRef]))("data")("children")

      for (postIndex <- 0 until posts(JSONParser.L).get.length) {
        val postNode = posts(postIndex)("data")
        val post = ModelParser.parse(ModelParser.T3)(postNode)
        if ((post.ups - post.downs) >= config.postMinKarma
          && post.date_posted.getTime() >= new Date().getTime() - config.postMaxAge) {
          EntityManager.insertPost(post, config.name)
          Log.Info(s"Mined post with id `${post.redditId}`")

          val commentJson = new JSONTraverser(Option(JSON.parseFull(client.get(s"comments/${post.redditId}.json")).get.asInstanceOf[AnyRef]))
          val commentNode = commentJson(1)("data")("children")

          for (comIndex <- 0 until commentNode(JSONParser.L).get.length) {
            try {
              val comment = ModelParser.parse(ModelParser.T1)(commentNode(comIndex)("data"))
              if (comment.gilded >= config.minGild
                && (comment.ups - comment.downs) >= config.commentMinKarma
                && comment.date_posted.getTime() >= new Date().getTime() - config.commentMaxAge) {
                EntityManager.insertPost(comment, config.name)
                Log.Info(s"Mined comment with id `${comment.redditId}`")
              }
            } catch {
              case nse: NoSuchElementException => Log.Warning("Could not parse a comment.")
            }
          }
        }
      }

    }

  }

}