package com.felixmilea.vorbit.reddit.mining

import com.felixmilea.vorbit.JSON.JSONTraverser
import com.felixmilea.vorbit.JSON.JSONParser
import com.felixmilea.vorbit.utils.App
import com.felixmilea.vorbit.JSON.JSONTraverser
import scala.collection.immutable.VectorBuilder
import com.felixmilea.vorbit.JSON.JSONTraverser
import com.felixmilea.vorbit.utils.Loggable

object MinerConfigParser {
  import MinerConfig._
  import JSONParser._

  def parse(json: JSONTraverser): MinerConfig = {
    try {
      new MinerConfig(json("dataset")().get, parseTasks(json("tasks")))
    } catch {
      case nse: NoSuchElementException => throw new MinerConfigParsingException(json("dataset")(), nse)
    }
  }

  def parseTasks(json: JSONTraverser): Seq[Task] = {
    val builder = new VectorBuilder[Task]

    for (i <- 0 until json(L).get.length) {
      val task = json(i)
      builder += new Task(
        name = task("name")().get,
        recurrence = task("recurrence")(I).get,
        targetType = TargetType.withName(task("targetType")().get),
        postType = PostType.withName(task("postType")().get),
        parsePostContent = task("parsePostContent")(B).get,
        postSort = PostSort.withName(task("postSort")().get),
        postListings = task("postListings")(I).get,
        time = task("time")().get,
        commentSort = CommentSort.withName(task("commentSort")().get),
        commentNesting = task("commentNesting")(I).get,
        targets = parseTargets(task("targets")))
    }

    return builder.result
  }

  def parseTargets(json: JSONTraverser): Seq[Target] = {
    val builder = new VectorBuilder[Target]

    for (i <- 0 until json(L).get.length) {
      val target = json(i)
      builder += new Target(
        units = target("units")(L).get.toSeq.map(_.toString),
        postConstraints = parsePostConstraints(target("postConstraints")),
        commentConstraints = parseCommentConstraints(target("commentConstraints")))
    }

    builder.result
  }

  def parsePostConstraints(json: JSONTraverser): Seq[PostConstraints] = {
    val builder = new VectorBuilder[PostConstraints]

    for (i <- 0 until json(L).get.length) {
      val con = json(i)
      builder += new PostConstraints(
        maxAge = con("maxAge")(I).get,
        minKarma = con("minKarma")(I).get)
    }

    builder.result
  }

  def parseCommentConstraints(json: JSONTraverser): Seq[CommentConstraints] = {
    val builder = new VectorBuilder[CommentConstraints]

    for (i <- 0 until json(L).get.length) {
      val con = json(i)
      builder += new CommentConstraints(
        maxAge = con("maxAge")(I).get,
        minKarma = con("minKarma")(I).get,
        minGild = con("minGild")(I).get)
    }

    builder.result
  }

}