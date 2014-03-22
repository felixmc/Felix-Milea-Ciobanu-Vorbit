package com.felixmilea.vorbit.reddit.mining

import com.felixmilea.vorbit.utils.JSON
import scala.collection.immutable.VectorBuilder
import com.felixmilea.vorbit.utils.Loggable
import com.felixmilea.vorbit.utils.JSONException

object MinerConfigParser {
  import com.felixmilea.vorbit.reddit.mining.config._

  def parse(json: JSON): MinerConfig = {
    //    try {
    new MinerConfig(json("dataset"), json("active"), parseTasks(json("tasks")))
    //    } catch {
    //      case je: JSONException => throw new MinerConfigParsingException(null, je)
    //      case nse: NoSuchElementException => throw new MinerConfigParsingException(json("dataset"), nse)
    //    }
  }

  def parseTasks(json: JSON): Seq[Task] = {
    val builder = new VectorBuilder[Task]

    for (task <- json) {
      builder += new Task(
        name = task("name"),
        recurrence = task("recurrence"),
        targetType = TargetType.withName(task("targetType")),
        postType = PostType.withName(task("postType")),
        parsePostContent = task("parsePostContent"),
        postSort = PostSort.withName(task("postSort")),
        postListings = task("postListings"),
        postLimit = task("postLimit"),
        time = task("time"),
        commentSort = CommentSort.withName(task("commentSort")),
        commentNesting = task("commentNesting"),
        targets = parseTargets(task("targets")))
    }

    return builder.result
  }

  def parseTargets(json: JSON): Seq[Target] = {
    val builder = new VectorBuilder[Target]

    for (target <- json) {
      builder += new Target(
        units = target("units").map(u => u.toString),
        postConstraints = parsePostConstraints(target("postConstraints")),
        commentConstraints = parseCommentConstraints(target("commentConstraints")))
    }

    builder.result
  }

  def parsePostConstraints(json: JSON): Seq[PostConstraints] = {
    val builder = new VectorBuilder[PostConstraints]

    for (con <- json) {
      builder += new PostConstraints(
        maxAge = con("maxAge"),
        minKarma = con("minKarma"))
    }

    builder.result
  }

  def parseCommentConstraints(json: JSON): Seq[CommentConstraints] = {
    val builder = new VectorBuilder[CommentConstraints]

    for (con <- json) {
      builder += new CommentConstraints(
        maxAge = con("maxAge"),
        minKarma = con("minKarma"),
        minGild = con("minGild"))
    }

    builder.result
  }

}