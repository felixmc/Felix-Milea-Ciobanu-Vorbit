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
      for (postJson <- listing.json.filterNot(p => p.data.stickied)) {
        val ignore = validator.ignoreSerious && postJson.data.link_flair_text.toString.toLowerCase.contains("serious")
        if (!ignore) {
          val post = ModelParser.parsePost(postJson.data)
          val isValid = isValidPost(post, validator)
          if (isValid) receiver ! ValidationResult(post, "listing", listing.tag)
        }
      }
    }
    case ValidateCommentListing(listing, validator, receiver) => {
      for (commentJson <- listing.json) {
        val comment = ModelParser.parseComment(commentJson.data)
        val isValid = isValidComment(comment, validator)
        if (isValid) receiver ! ValidationResult(comment, "post", listing.tag)
      }
    }
    case ValidatePostResult(postResult, validator, receiver) => {
      var hasGoodChildren = false
      val comments = postResult.json(1).data.children

      for (commentJSON <- comments) {
        if (commentJSON.kind.toString == "t1") {
          val comment = ModelParser.parseComment(commentJSON.data)
          val isGood = isValidComment(comment, validator)
          if (isGood) {
            hasGoodChildren = true
            receiver ! ValidationResult(comment, "post", postResult.tag)

            //App.actor("DataSetManager") ! DataSetManager.PersistPost(comment, config.name)
            //          //          if (nesting >= 0 && !commentsNode(comIndex)("data")("replies")().get.isEmpty)
            //          //            mineThreadComments(commentsNode(comIndex)("data")("replies"), nesting - 1)
          }
        }
      }

      if (hasGoodChildren) {
        val post = ModelParser.parsePost(postResult.json(0).data.children(0).data)
        receiver ! ValidationResult(post, "post", postResult.tag)
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
    if (comment.content == "[deleted]") return false
    for (crit <- config.criteria) {
      val isValid = criteriaValidate(comment, crit) && (comment.gilded >= crit.minGild)
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
  case class ValidateListingPosts(listing: PostListingResult, validator: PostValidator, receiver: ActorSelection) extends WorkCommand
  case class ValidatePostResult(post: PostResult, validator: CommentValidator, receiver: ActorSelection) extends WorkCommand
  case class ValidateCommentListing(listing: CommentListingResult, validator: CommentValidator, receiver: ActorSelection) extends WorkCommand

  case class ValidationResult(post: RedditPost, source: String, tag: String = "")

  case class ValidationCriteria(minKarma: Int = 0, maxKarma: Int = 0, minAge: Int = 0, maxAge: Int = 0, minGild: Int = 0)
  case class PostValidator(postType: PostType.PostType, criteria: Seq[ValidationCriteria], ignoreSerious: Boolean = false)
  case class CommentValidator(criteria: Seq[ValidationCriteria])
}