package com.felixmilea.vorbit.reddit.mining

import com.felixmilea.vorbit.reddit.connectivity.Client
import com.felixmilea.vorbit.reddit.models.Post
import com.felixmilea.vorbit.reddit.models.Comment
import java.util.Date
import com.felixmilea.vorbit.utils.Log

abstract class MiningEngine(protected val config: MinerConfig) {
  protected val client = new Client

  def mine()

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

  def subredditUrl(name: String): String = {
    return s"r/$name/${config.postSort}.json?limit=100"
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