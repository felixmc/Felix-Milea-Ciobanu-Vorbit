package com.felixmilea.vorbit.reddit.models

import java.util.Date

class Post(redditId: String, author: String, subreddit: String, val title: String,
  content: String, ups: Int, downs: Int, children_count: Int, date_posted: Date, val postType: String)
  extends RedditPost(redditId, author, subreddit, content, ups, downs, children_count, date_posted) {

  override def toString(): String = {
    val sb = new StringBuilder

    sb ++= s"reddit id: $redditId\n"
    sb ++= s"author: $author\n"
    sb ++= s"subreddit: $subreddit\n"
    sb ++= s"title: $title\n"
    sb ++= s"content: $content\n"
    sb ++= s"ups: $ups\n"
    sb ++= s"downs: $downs\n"
    sb ++= s"children: $children_count\n"
    sb ++= s"date posted: $date_posted"

    sb.mkString
  }
}