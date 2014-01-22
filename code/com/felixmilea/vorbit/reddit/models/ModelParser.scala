package com.felixmilea.vorbit.reddit.models

import scala.util.parsing.json.JSON
import com.felixmilea.vorbit.JSON.JSONTraverser
import java.util.Date
import com.felixmilea.vorbit.JSON.JSONParser
import scala.util.parsing.json.JSONObject
import com.felixmilea.vorbit.utils.Log

object ModelParser {

  def parse[T <: RedditThing](extractor: Extractor[T])(json: JSONTraverser): T = extractor.unapply(json)

  abstract class Extractor[T <: RedditThing] {
    def unapply(json: JSONTraverser): T
  }

  object T1 extends Extractor[Comment] {
    def unapply(json: JSONTraverser): Comment = {
      try {
        val replies: Int = json("replies")("data")("children")(JSONParser.L) match {
          case Some(list) => list.length
          case _ => 0
        }

        new Comment(
          redditId = json("id")().get,
          parentId = json("parent_id")().get,
          subreddit = json("subreddit")().get,
          ups = json("ups")(JSONParser.I).get, downs = json("downs")(JSONParser.I).get,
          gilded = json("gilded")(JSONParser.I).get, children_count = replies,
          date_posted = new Date(json("created")(JSONParser.D).get.toLong * 1000),
          content = json("body")().get,
          author = json("author")().get)
      } catch {
        case nse: NoSuchElementException => {
          throw new RedditPostParseException(json)
        }
      }
    }
  }

  object T3 extends Extractor[Post] {
    def unapply(json: JSONTraverser): Post = {
      try {
        new Post(redditId = json("id")().get, author = json("author")().get, subreddit = json("subreddit")().get,
          title = json("title")().get, content = json("selftext")().get, ups = json("ups")(JSONParser.I).get,
          downs = json("downs")(JSONParser.I).get, children_count = json("num_comments")(JSONParser.I).get,
          date_posted = new Date(json("created")(JSONParser.D).get.toLong * 1000),
          postType = if (json("is_self")(JSONParser.B).get) "self" else "link")
      } catch {
        case nse: NoSuchElementException => {
          throw new RedditPostParseException(json)
        }
      }
    }
  }

}