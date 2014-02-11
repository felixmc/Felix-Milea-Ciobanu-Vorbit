package com.felixmilea.vorbit.reddit.mining

class MinerConfig(
  val dataset: String,
  val tasks: Seq[MinerConfig.Task])

object MinerConfig {

  class Task(
    val name: String,
    val recurrence: Int,
    val targetType: TargetType.TargetType,
    val postType: PostType.PostType,
    val parsePostContent: Boolean,
    val postSort: PostSort.PostSort,
    val postListings: Int,
    val time: String,
    val commentSort: CommentSort.CommentSort,
    val commentNesting: Int,
    val targets: Seq[Target])

  object CommentSort extends Enumeration {
    type CommentSort = Value
    val Best = Value("best")
    val Top = Value("top")
    val Hot = Value("hot")
    val New = Value("new")
    val Controversial = Value("controversial")
    val Old = Value("old")
  }

  object PostSort extends Enumeration {
    type PostSort = Value
    val Hot = Value("hot")
    val New = Value("new")
    val Rising = Value("rising")
    val Controversial = Value("controversial")
    val Top = Value("top")
  }

  object PostType extends Enumeration {
    type PostType = Value
    val Self = Value("self")
    val Link = Value("link")
    val Both = Value("both")
  }

  object TargetType extends Enumeration {
    type TargetType = Value
    val Subreddit = Value("subreddit")
    val User = Value("user")
  }

  class Target(val units: Seq[String], val postConstraints: Seq[PostConstraints], val commentConstraints: Seq[CommentConstraints])

  class PostConstraints(
    val maxAge: Int = 0,
    val minKarma: Int = 0)

  class CommentConstraints(
    maxAge: Int = 0,
    minKarma: Int = 0,
    val minGild: Int = 0)
    extends PostConstraints(maxAge, minKarma)
}