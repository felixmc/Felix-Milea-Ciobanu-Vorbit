package com.felixmilea.vorbit.actors

import java.util.Date
import akka.actor.ActorSelection
import com.felixmilea.vorbit.actors.ManagedActor.WorkCommand
import com.felixmilea.vorbit.actors.RedditDownloader._
import com.felixmilea.vorbit.reddit.mining.config.PostType
import com.felixmilea.vorbit.reddit.models.ModelParser
import com.felixmilea.vorbit.reddit.models.Post
import com.felixmilea.vorbit.reddit.models.Comment
import com.felixmilea.vorbit.utils.AppUtils
import com.felixmilea.vorbit.reddit.models.RedditPost

class RedditPostValidator extends ManagedActor {
  import RedditPostValidator._

  def doReceive = {
    case ValidateListingPosts(listing, validator, receiver) => {
      for (postJson <- listing.json.filter(p => p.data.stickied)) {
        val post = ModelParser.parsePost(postJson.data)
        if (isValidPost(post, validator)) receiver ! post
      }
    }
    case ValidatePostResult(postJson, validator, receiver) => {
      var hasGoodChildren = false
      val comments = postJson.json(1).data.children

      for (commentJSON <- comments) {
        if (commentJSON.kind.toString == "t1") {
          val comment = ModelParser.parseComment(commentJSON.data)
          if (isValidComment(comment, validator)) {
            hasGoodChildren = true
            receiver ! comment

            //App.actor("DataSetManager") ! DataSetManager.PersistPost(comment, config.name)
            //          //          if (nesting >= 0 && !commentsNode(comIndex)("data")("replies")().get.isEmpty)
            //          //            mineThreadComments(commentsNode(comIndex)("data")("replies"), nesting - 1)
          }
        }
      }

      if (hasGoodChildren) {
        val post = ModelParser.parsePost(postJson.json(0).data.children(0).data)
        receiver ! post
      }
    }
  }

  def isValidPost(post: Post, config: PostValidator): Boolean = {
    if (config.postType != PostType.Both && config.postType != PostType.withName(post.postType)) return false
    for (crit <- config.criteria) {
      val isValid = criteriaValidate(post, crit)
      if (!isValid) return isValid
    }

    return true
  }

  def isValidComment(comment: Comment, config: CommentValidator): Boolean = {
    for (crit <- config.criteria) {
      val isValid = criteriaValidate(comment, crit) && (comment.gilded < crit.minGild)
      if (!isValid) return isValid
    }

    return true
  }

  def criteriaValidate(post: RedditPost, crit: ValidationCriteria): Boolean = {
    if (crit.minKarma != 0 && post.karma < crit.minKarma) return false
    if (crit.maxKarma != 0 && post.karma > crit.maxKarma) return false
    if (crit.maxAge != 0 && (post.date_posted before new Date(new Date().getTime() - crit.maxAge))) return false
    if (crit.minAge != 0 && (post.date_posted after new Date(new Date().getTime() - crit.minAge))) return false
    return true
  }

}

object RedditPostValidator {
  case class ValidateListingPosts(listing: ListingResult, validator: PostValidator, receiver: ActorSelection) extends WorkCommand
  case class ValidatePostResult(post: PostResult, validator: CommentValidator, receiver: ActorSelection) extends WorkCommand

  case class ValidationCriteria(minKarma: Int = 0, maxKarma: Int = 0, minAge: Int = 0, maxAge: Int = 0, minGild: Int = 0)
  case class PostValidator(postType: PostType.PostType, criteria: Seq[ValidationCriteria])
  case class CommentValidator(criteria: Seq[ValidationCriteria])
}