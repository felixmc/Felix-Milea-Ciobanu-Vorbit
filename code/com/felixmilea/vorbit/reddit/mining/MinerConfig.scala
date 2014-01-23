package com.felixmilea.vorbit.reddit.mining

import com.felixmilea.vorbit.JSON.JSONTraverser
import com.felixmilea.vorbit.JSON.JSONParser
import com.felixmilea.vorbit.JSON.JSONTraverser

class MinerConfig(
  val name: String = "Miner",
  val unitType: String = "subreddit",
  val units: List[String],
  val postType: String = "both",
  val postSort: String = "hot",
  val time: String = "",
  val postConstraints: List[MinerConfig.PostConstraints],
  val commentConstraints: List[MinerConfig.CommentConstraints],
  val commentSort: String = "top",
  val commentNestingLevel: Int = 0,
  val pages: Int = 5)

object MinerConfig {
  class PostConstraints(
    val maxAge: Int = 0,
    val minKarma: Int = 0) {}

  class CommentConstraints(
    maxAge: Int = 0,
    minKarma: Int = 0,
    val minGild: Int = 0)
    extends PostConstraints(maxAge, minKarma) {}

  def parse(json: JSONTraverser): MinerConfig = {
    val postConstraints = json("config")("postConstraints")(JSONParser.L).get.map(c => {
      val json = new JSONTraverser(c.asInstanceOf[Option[AnyRef]])
      new PostConstraints(json("maxAge")(JSONParser.I).get, json("minKarma")(JSONParser.I).get)
    })
    val commentConstraints = json("config")("commentConstraints")(JSONParser.L).get.map(c => {
      val json = new JSONTraverser(c.asInstanceOf[Option[AnyRef]])
      new CommentConstraints(json("maxAge")(JSONParser.I).get, json("minKarma")(JSONParser.I).get, json("minGild")(JSONParser.I).get)
    })

    return new MinerConfig(
      name = json("name")().get,
      unitType = json("config")("unitType")().get,
      units = json("config")("units")(JSONParser.L).get.asInstanceOf[List[Option[String]]].map(o => o.get),
      postType = json("config")("postType")().get,
      postSort = json("config")("postSort")().get,
      time = json("config")("time")().get,
      postConstraints = postConstraints,
      commentConstraints = commentConstraints,
      commentSort = json("config")("commentSort")().get,
      commentNestingLevel = json("config")("commentNestingLevel")(JSONParser.I).get)
  }
}