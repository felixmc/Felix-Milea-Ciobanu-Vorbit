package com.felixmilea.vorbit.analysis

import com.felixmilea.vorbit.data.DBConnection
import com.felixmilea.vorbit.utils.ConfigManager
import com.felixmilea.vorbit.reddit.models.Comment
import scala.collection.mutable.ArrayBuffer
import com.felixmilea.vorbit.utils.Log

object Analyzer extends App {
  ConfigManager.init
  val postCount = 20
  val posts = getPosts(postCount).map(p => p.content).filter(s => TextUnitParser.isGoodSource(s))
  val parser = new TextUnitParser

  val post = "http://something.com/ dsafdsa is a URL and so is https://asdf.net/?dsaf=sdfa "

  sep

  Log.Info(post)

  sep

  printNgrams(parser.parse(post))

  //  for (post <- posts) {
  //    val ngrams = parser.parse(post)
  //
  //    println(s"$post")
  //
  //  }

  def sep = Log.Warning("=" * 180)

  def printNgrams(ngrams: Seq[String]) {
    val sb = new StringBuilder

    for (i <- 0 until ngrams.length) {
      //      sb ++= s"[${Console.WHITE}${ngrams(i)}${Console.CYAN}] "
      sb ++= s"[${ngrams(i)}] "
      if ((i + 1) % 20 == 0) {
        Log.Info(sb.mkString)
        sb.clear
      }
    }

    if (!sb.isEmpty)
      Log.Info(sb.mkString)

    sep
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