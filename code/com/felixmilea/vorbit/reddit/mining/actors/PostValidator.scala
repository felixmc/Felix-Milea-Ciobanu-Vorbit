package com.felixmilea.vorbit.reddit.mining.actors

import java.util.Date
import com.felixmilea.vorbit.utils.JSON
import com.felixmilea.vorbit.reddit.mining.config.ConfigState
import com.felixmilea.vorbit.reddit.mining.actors.RedditDownloader._
import com.felixmilea.vorbit.reddit.models.ModelParser
import com.felixmilea.vorbit.reddit.models.Post
import com.felixmilea.vorbit.reddit.mining.config.PostType
import com.felixmilea.vorbit.reddit.mining.actors.ManagedActor.MinerCommand
import com.felixmilea.vorbit.reddit.models.Comment
import com.felixmilea.vorbit.utils.AppUtils
import com.felixmilea.vorbit.reddit.mining.actors.PostProcessor._

class PostValidator extends ManagedActor {
  import PostValidator._

  private[this] lazy val validator = AppUtils.actor(self.path.parent.parent.child("validator"))
  private[this] lazy val downloader = AppUtils.actor(self.path.parent.parent.child("downloader"))
  private[this] lazy val processor = AppUtils.actor(self.path.parent.parent.child("postProcessor"))

  def doReceive = {
    case ListingResult(json, conf) => {
      for (postJson <- json) {
        if (!postJson.data.stickied) {
          val post = ModelParser.parsePost(postJson.data)
          if (isValidPost(post, conf)) {
            downloader ! MinerCommand(DownloadRequest(Post(post.redditId), validator), conf)
          }
        }
      }
    }
    case PostResult(json, conf) => {
      var hasGoodChildren = false
      val comments = json(1).data.children

      for (commentJSON <- comments) {
        if (commentJSON.kind.toString == "t1") {
          val comment = ModelParser.parseComment(commentJSON.data)
          if (isValidComment(comment, conf)) {
            hasGoodChildren = true

            processor ! MinerCommand(ProcessPost(comment), conf)

            //App.actor("DataSetManager") ! DataSetManager.PersistPost(comment, config.name)
            //          //          if (nesting >= 0 && !commentsNode(comIndex)("data")("replies")().get.isEmpty)
            //          //            mineThreadComments(commentsNode(comIndex)("data")("replies"), nesting - 1)
          }
        }
      }

      if (hasGoodChildren) {
        val post = ModelParser.parsePost(json(0).data.children(0).data)
        processor ! MinerCommand(ProcessPost(post), conf)
      }
    }
  }

  def isValidPost(post: Post, config: ConfigState): Boolean = {
    if (config.task.postType != PostType.Both && config.task.postType != PostType.withName(post.postType)) return false
    for (ps <- config.target.postConstraints) {
      if (post.karma < ps.minKarma) return false
      if (ps.maxAge != 0 && (post.date_posted before new Date(new Date().getTime() - ps.maxAge))) return false
    }

    return true
  }

  def isValidComment(comment: Comment, config: ConfigState): Boolean = {
    for (cs <- config.target.commentConstraints) {
      if (comment.gilded < cs.minGild) return false
      if (comment.karma < cs.minKarma) return false
      if (cs.maxAge != 0 && (comment.date_posted before new Date(new Date().getTime() - cs.maxAge))) return false
    }

    return true
  }

}

object PostValidator {

}