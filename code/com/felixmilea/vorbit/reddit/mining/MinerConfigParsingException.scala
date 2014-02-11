package com.felixmilea.vorbit.reddit.mining

class MinerConfigParsingException(miner: Option[String], nse: NoSuchElementException)
  extends RuntimeException(s"MinerConfigParsing encountered a parsing error while parsing miner '${miner.getOrElse("[Not Found]")}'.")