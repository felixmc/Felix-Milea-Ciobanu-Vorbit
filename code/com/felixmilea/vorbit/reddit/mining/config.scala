package com.felixmilea.vorbit.reddit.mining

package config {

  class MinerConfigParsingException(miner: String, e: Exception)
    extends RuntimeException(s"MinerConfigParsing encountered a parsing error while parsing miner '$miner': " + e)

  case class MinerConfig(
    dataset: String,
    active: Boolean,
    tasks: Seq[Task])

  case class TaskConfig(
    dataset: String,
    task: Task)

  case class ConfigState(dataset: String, task: Task, target: Target)

  case class Task(
    name: String,
    recurrence: Int,
    targetType: TargetType.TargetType,
    postType: PostType.PostType,
    parsePostContent: Boolean,
    postSort: PostSort.PostSort,
    postListings: Int,
    postLimit: Int,
    time: String,
    commentSort: CommentSort.CommentSort,
    commentNesting: Int,
    targets: Seq[Target])

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

  case class Target(
    units: Seq[String],
    postConstraints: Seq[PostConstraints],
    commentConstraints: Seq[CommentConstraints])

  case class PostConstraints(
    maxAge: Int = 0,
    minKarma: Int = 0)

  case class CommentConstraints(
    maxAge: Int = 0,
    minKarma: Int = 0,
    minGild: Int = 0)

}