package com.felixmilea.vorbit.actors

import com.felixmilea.vorbit.actors.RedditDownloader._
import com.felixmilea.vorbit.actors.RedditPostValidator._
import com.felixmilea.vorbit.utils.JSON
import com.felixmilea.vorbit.posting.PostingManager
import com.felixmilea.vorbit.reddit.mining.config.PostType
import com.felixmilea.vorbit.utils.AppUtils
import com.felixmilea.vorbit.actors.PostRecorder.PostingTarget
import com.felixmilea.vorbit.actors.PostRecorder.CheckTarget
import com.felixmilea.vorbit.actors.Composer.GenerateReply

class PostingCoordinator(config: JSON, task: JSON) extends ManagedActor {
  private[this] val dataset = AppUtils.config.persistence.data.datasets(config.corpus.dataset)
  private[this] val subset = AppUtils.config.persistence.data.subsets(config.corpus.subset)
  private[this] val edition = AppUtils.config.persistence.data.editions(config.corpus.edition)

  private[this] lazy val validator = sibling(PostingManager.Names.validator)
  private[this] lazy val recorder = sibling(PostingManager.Names.recorder)
  private[this] lazy val composer = sibling(config.name + "-" + PostingManager.Names.composer)
  private[this] lazy val posters = sibling(PostingManager.Names.poster)

  def doReceive = {
    case l: PostListingResult => {
      val criteria = task.targets(l.tag.toInt).constraints.map(ps => new ValidationCriteria(minKarma = ps.minKarma, maxAge = ps.maxAge))
      val postType = PostType.withName(task.postType)
      validator ! ValidateListingPosts(l, PostValidator(postType, criteria, true), this.selfSelection)
    }
    case ValidationResult(post, source, tag) => {
      recorder ! CheckTarget(PostingTarget(post, dataset, subset, edition), this.selfSelection)
    }
    case t: PostingTarget => {
      composer ! GenerateReply(t, posters)
    }
  }

}