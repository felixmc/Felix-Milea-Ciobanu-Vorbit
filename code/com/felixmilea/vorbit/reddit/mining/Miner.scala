package com.felixmilea.vorbit.reddit.mining

import com.felixmilea.vorbit.reddit.connectivity.Client
import scala.util.parsing.json.JSON
import com.felixmilea.vorbit.JSON.JSONParser
import com.felixmilea.vorbit.JSON.JSONTraverser
import com.felixmilea.vorbit.reddit.models.ModelParser
import com.felixmilea.vorbit.reddit.models.RedditPostParseException
import com.felixmilea.vorbit.reddit.models.Comment
import com.felixmilea.vorbit.reddit.models.Post
import com.felixmilea.vorbit.data.EntityManager
import com.felixmilea.vorbit.analysis.WordParser
import com.felixmilea.vorbit.utils.Log
import java.util.Date

class Miner(private val config: MinerConfig) extends Thread {
  private val DELAY = (1000 * 60) * 5
  private val engine = MiningEngine.get(config)

  EntityManager.setupMiner(config.name)

  override def run() {
    while (true) {
      Log.Info(s"Starting data mining operation `${config.name}`")
      engine.mine
      Log.Info(s"Pausing data mining operation `${config.name}` for $DELAY ms")
      Thread.sleep(DELAY)
    }
  }

}