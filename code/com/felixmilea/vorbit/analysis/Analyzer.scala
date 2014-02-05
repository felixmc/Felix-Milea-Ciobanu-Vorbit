package com.felixmilea.vorbit.analysis

import com.felixmilea.vorbit.data.DBConnection
import com.felixmilea.vorbit.utils.ConfigManager
import com.felixmilea.vorbit.reddit.models.Comment
import scala.collection.mutable.ArrayBuffer
import com.felixmilea.vorbit.utils.Log
import com.felixmilea.vorbit.utils.Loggable

object Analyzer extends App with Loggable {
  val postCount = 20
  val posts = getPosts(postCount).map(p => p.content).filter(s => TextUnitParser.isGoodSource(s))
  val parser = new TextUnitParser

  for (post <- posts) {
    val ngrams = parser.parse(post)
    printNgrams(ngrams)
  }

  def sep = Warning("=" * 180)

  def printNgrams(ngrams: Seq[String]) {
    sep
    val sb = new StringBuilder

    for (i <- 0 until ngrams.length) {
      sb ++= s"[${ngrams(i)}] "
      if ((i + 1) % 20 == 0) {
        Info(sb.mkString)
        sb.clear
      }
    }

    if (!sb.isEmpty)
      Info(sb.mkString)
  }

  def getPosts(count: Int = 100): Vector[Comment] = {
    val db = new DBConnection(true)
    val rows = db.executeQuery(s"SELECT * FROM `mdt_answerbot_a1` WHERE `type` = 't1' AND `subreddit` = 'explainlikeimfive' ORDER BY `date_posted` LIMIT $count")

    val posts = new ArrayBuffer[Comment]

    while (rows.next()) {
      posts += new Comment(
        redditId = rows.getString("reddit_id"),
        parentId = rows.getString("parent"),
        subreddit = rows.getString("subreddit"),
        ups = rows.getInt("ups"),
        downs = rows.getInt("downs"),
        children_count = rows.getInt("downs"),
        gilded = rows.getInt("gilded"),
        content = rows.getString("content"),
        author = rows.getString("author"),
        date_posted = rows.getDate("date_posted"))
    }

    return posts.toVector
  }

}