package com.felixmilea.vorbit.main

import com.felixmilea.vorbit.utils.Loggable
import com.felixmilea.vorbit.http.PathIterator
import com.felixmilea.vorbit.utils.JSON
import com.felixmilea.vorbit.utils.AppUtils
import com.felixmilea.vorbit.reddit.mining.MinerConfigParser

object SimpleTest extends App with Loggable {

  //  val data = Seq("answers", "smart", "something")
  //
  //  val json = JSON.makeJSON(data)
  //
  //  println(json)

  val data = "{\"dataset\":\"maybeHumanBot\",\"active\":\"true\",\"tasks\":[{\"name\":\"top\",\"recurrence\":0,\"targetType\":\"subreddit\",\"postType\":\"both\",\"postSort\":\"top\",\"parsePostContent\":\"false\",\"time\":\"all\",\"postListings\":5,\"postLimit\":100,\"commentSort\":\"top\",\"commentNesting\":1,\"targets\":[{\"units\":[\"funny\",\"pics\"],\"commentConstraints\":[{\"minKarma\":500,\"minGild\":0,\"maxAge\":0}],\"postConstraints\":[{\"minKarma\":3000,\"maxAge\":0}]},{\"units\":[\"askReddit\"],\"commentConstraints\":[{\"minKarma\":1500,\"minGild\":0,\"maxAge\":0}],\"postConstraints\":[{\"minKarma\":2000,\"maxAge\":0}]}]},{\"name\":\"hot\",\"recurrence\":1800000,\"targetType\":\"subreddit\",\"postType\":\"both\",\"postSort\":\"hot\",\"parsePostContent\":\"false\",\"time\":\"all\",\"postListings\":5,\"postLimit\":50,\"commentSort\":\"top\",\"commentNesting\":1,\"targets\":[{\"units\":[\"pics\",\"funny\"],\"commentConstraints\":[{\"minKarma\":500,\"minGild\":0,\"maxAge\":0}],\"postConstraints\":[{\"minKarma\":2000,\"maxAge\":0}]},{\"units\":[\"askReddit\"],\"commentConstraints\":[{\"minKarma\":500,\"minGild\":0,\"maxAge\":0}],\"postConstraints\":[{\"minKarma\":1000,\"maxAge\":0}]}]}]}"

  val json = JSON(data)

  val config = MinerConfigParser.parse(json)

  println(config)

  //  println(json)

  //  Info(AppUtils.config.persistence.data.datasets)

}