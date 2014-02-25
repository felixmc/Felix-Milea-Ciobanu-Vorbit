package com.felixmilea.vorbit.actors

import akka.actor.ActorRef
import com.felixmilea.vorbit.actors.ManagedActor.Forward
import com.felixmilea.vorbit.actors.RedditDownloader._
import com.felixmilea.vorbit.actors.RedditPostValidator._
import com.felixmilea.vorbit.actors.PostProcessor._
import com.felixmilea.vorbit.actors.TextUnitProcessor.RecordText
import com.felixmilea.vorbit.reddit.mining.config.TaskConfig
import com.felixmilea.vorbit.reddit.mining.RedditMiningManager
import com.felixmilea.vorbit.utils.AppUtils
import com.felixmilea.vorbit.reddit.models.Post

class MiningCoordinator(manager: ActorRef, config: TaskConfig) extends ManagedActor {
  private[this] val dataset = AppUtils.config.persistence.data.datasets(config.dataset)
  private[this] lazy val downloader = sibling(RedditMiningManager.Names.download)
  private[this] lazy val validator = sibling(RedditMiningManager.Names.validator)
  private[this] lazy val postProcessor = sibling(RedditMiningManager.Names.post)
  private[this] lazy val textProcessor = sibling(RedditMiningManager.Names.text)

  private[this] val (parentsSubset, childrenSubset) = (AppUtils.config.persistence.data.subsets("parents"), AppUtils.config.persistence.data.subsets("children"))

  def doReceive = {
    case l: ListingResult => {
      val criteria = config.task.targets(l.tag.toInt).postConstraints.map(ps => new ValidationCriteria(minKarma = ps.minKarma, maxAge = ps.maxAge))
      validator ! ValidateListingPosts(l, PostValidator(config.task.postType, criteria), this.selfSelection)
    }
    case pr: PostResult => {
      val criteria = config.task.targets(pr.tag.toInt).commentConstraints.map(ps => new ValidationCriteria(minKarma = ps.minKarma, maxAge = ps.maxAge, minGild = ps.minGild))
      validator ! ValidatePostResult(pr, CommentValidator(criteria), this.selfSelection)
    }
    case ValidationResult(post, source, tag) => source match {
      case "listing" => {
        downloader ! DownloadRequest(Post(post.redditId, config.task.commentSort), this.selfSelection, tag)
      }
      case "post" => {
        val subset = if (post.isInstanceOf[Post]) parentsSubset else childrenSubset
        postProcessor ! ProcessPost(post, dataset, subset, Forward(RecordText(if (post.isInstanceOf[Post]) post.asInstanceOf[Post].title else post.content, dataset, subset), textProcessor))
      }
    }

  }

}