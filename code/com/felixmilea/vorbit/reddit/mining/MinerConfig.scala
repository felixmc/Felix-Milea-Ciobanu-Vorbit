package com.felixmilea.vorbit.reddit.mining

class MinerConfig {
  var subredditMode = MinerConfig.SubredditMode.Blacklist
  var subreddits = List[String]()
  var postMaxAge = 0
  var commentMaxAge = 0
  var postMinKarma = 0
  var commentMinKarma = 0
  var minGild = 0
}

object MinerConfig {
  object SubredditMode extends Enumeration {
    type SubredditMode = Value
    val Whitelist, Blacklist = Value
  }
}