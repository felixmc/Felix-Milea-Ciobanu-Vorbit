package com.felixmilea.vorbit.reddit.mining

import com.felixmilea.vorbit.JSON.JSONTraverser
import com.felixmilea.vorbit.JSON.JSONParser

class MinerConfig {
  var subredditMode = MinerConfig.SubredditMode.Whitelist
  var subreddits = List[String]()
  var postMaxAge = 0
  var commentMaxAge = 0
  var postMinKarma = 0
  var commentMinKarma = 0
  var minGild = 0
  var account = ""
  var name = ""
}

object MinerConfig {
  object SubredditMode extends Enumeration {
    type SubredditMode = Value
    val Whitelist, Blacklist = Value
  }

  def parse(json: JSONTraverser): MinerConfig = {
    return new MinerConfig {
      subreddits = json("config")("subreddits")(JSONParser.L).get.asInstanceOf[List[String]]
      postMaxAge = json("config")("postMaxAge")(JSONParser.I).get
      commentMaxAge = json("config")("commentMaxAge")(JSONParser.I).get
      postMinKarma = json("config")("postMinKarma")(JSONParser.I).get
      commentMinKarma = json("config")("commentMinKarma")(JSONParser.I).get
      minGild = json("config")("minGild")(JSONParser.I).get
      account = json("account")().get
      name = json("name")().get
    }
  }
}