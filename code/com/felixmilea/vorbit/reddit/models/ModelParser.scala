package com.felixmilea.vorbit.reddit.models

import java.util.Date
import com.felixmilea.vorbit.utils.JSON
import com.felixmilea.vorbit.utils.Log

object ModelParser {

  def parseComment(json: JSON): Comment = T1.unapply(json)
  def parsePost(json: JSON): Post = T3.unapply(json)

  abstract class Extractor[T <: RedditThing] {
    def unapply(json: JSON): T
  }

  object T1 extends Extractor[Comment] {
    def unapply(json: JSON): Comment = {
      try {
        new Comment(
          redditId = json("id"),
          parentId = json("parent_id"),
          subreddit = json("subreddit"),
          ups = json("ups"),
          downs = json("downs"),
          gilded = json("gilded"),
          children_count = if (json("replies").has("data")) json("replies")("data")("children").length else 0,
          date_posted = new Date(json("created").toInt.toLong * 1000),
          content = json("body"),
          author = json("author"))
      } catch {
        case nse: NoSuchElementException => {
          throw new RedditPostParseException(json)
        }
      }
    }
  }

  object T3 extends Extractor[Post] {
    def unapply(json: JSON): Post = {
      try {
        new Post(redditId = json("id"),
          author = json("author"),
          subreddit = json("subreddit"),
          title = json("title"),
          content = json("selftext"),
          ups = json("ups"),
          downs = json("downs"),
          children_count = json("num_comments"),
          date_posted = new Date(json("created").toInt.toLong * 1000),
          postType = if (json("is_self")) "self" else "link")
      } catch {
        case nse: NoSuchElementException => {
          throw new RedditPostParseException(json)
        }
      }
    }
  }

}