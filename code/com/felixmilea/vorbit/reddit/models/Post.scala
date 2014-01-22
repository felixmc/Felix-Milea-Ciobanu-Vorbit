package com.felixmilea.vorbit.reddit.models

import java.util.Date

class Post(redditId: String, author: String, subreddit: String, val title: String,
  content: String, ups: Int, downs: Int, children_count: Int, date_posted: Date, val postType: String)
  extends RedditPost(redditId, author, subreddit, content, ups, downs, children_count, date_posted) {

  override def toString(): String = return s"$subreddit/$redditId"
}