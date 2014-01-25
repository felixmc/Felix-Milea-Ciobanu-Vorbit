package com.felixmilea.vorbit.reddit.mining

import java.util.Date

import com.felixmilea.vorbit.JSON.JSONParser
import com.felixmilea.vorbit.JSON.JSONTraverser
import com.felixmilea.vorbit.analysis.WordParser
import com.felixmilea.vorbit.data.EntityManager
import com.felixmilea.vorbit.reddit.connectivity.Client
import com.felixmilea.vorbit.reddit.models.Comment
import com.felixmilea.vorbit.reddit.models.ModelParser
import com.felixmilea.vorbit.reddit.models.Post
import com.felixmilea.vorbit.reddit.models.RedditPost
import com.felixmilea.vorbit.utils.Log

abstract class MiningEngine(protected val config: MinerConfig) {
  protected val client = new Client

  def mine()

  protected def mineThreadComments(json: JSONTraverser, nesting: Int = config.commentNestingLevel) {
    val commentsNode = json("data")("children")

    for (comIndex <- 0 until commentsNode(JSONParser.L).get.length) {
      if (commentsNode(comIndex)("kind")().get == "t1") {
        val comment = ModelParser.parse(ModelParser.T1)(commentsNode(comIndex)("data"))
        if (isValidComment(comment)) {
          if (EntityManager.persistPost(comment, config.name)) logMined(comment)
          if (nesting >= 0 && !commentsNode(comIndex)("data")("replies")().get.isEmpty)
            mineThreadComments(commentsNode(comIndex)("data")("replies"), nesting - 1)
        }
      }
    }
  }

  protected def isValidPost(post: Post): Boolean = {
    if (config.postType != "both" && config.postType != post.postType) return false
    for (ps <- config.postConstraints) {
      if (post.karma < ps.minKarma) return false
      if (ps.maxAge != 0 && (post.date_posted before new Date(new Date().getTime() - ps.maxAge))) return false
    }

    return true
  }

  protected def isValidComment(comment: Comment): Boolean = {
    for (cs <- config.commentConstraints) {
      if (comment.gilded < cs.minGild) return false
      if (comment.karma < cs.minKarma) return false
      if (cs.maxAge != 0 && (comment.date_posted before new Date(new Date().getTime() - cs.maxAge))) return false
    }

    return true
  }

  def logMined(post: RedditPost) {
    //    val content = (if (post.isInstanceOf[Post]) post.asInstanceOf[Post].title + " " else "") + post.content
    //    Log.Debug(s"\tMined post `$post` | ngrams parsed: ${WordParser.parseAsText(content)}")
  }

  def subredditUrl(name: String, after: String): String = {
    val vars = (if (!config.time.isEmpty) s"&t=${config.time}" else "") + (if (after.isEmpty()) "" else s"&after=t3_$after")
    return s"r/$name/${config.postSort}/.json?limit=100$vars"
  }

  def commentsUrl(redditId: String): String = {
    return s"comments/${redditId}.json?sort=${config.commentSort}"
  }

}

object MiningEngine {
  def get(config: MinerConfig): MiningEngine = config.unitType match {
    case "subreddit" => new SubredditMiner(config)
    case "user" => new UserMiner(config)
    case _ => throw new IllegalArgumentException(s"'${config.unitType}' is not a valid unit type.")
  }
}