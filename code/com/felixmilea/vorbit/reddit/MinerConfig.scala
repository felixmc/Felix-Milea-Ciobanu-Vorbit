package com.felixmilea.vorbit.reddit

class MinerConfig {
  var subredditMode = MinerConfig.SubredditMode.Blacklist
  var subreddits = List[String]()

}

object MinerConfig {

  object SubredditMode extends Enumeration {
    type Direction = Value
    val Whitelist, Blacklist = Value
  }

}