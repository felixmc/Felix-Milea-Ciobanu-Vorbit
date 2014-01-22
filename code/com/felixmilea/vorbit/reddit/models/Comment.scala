package com.felixmilea.vorbit.reddit.models

import java.util.Date

class Comment(redditId: String, val parentId: String, author: String, subreddit: String,
  content: String, ups: Int, downs: Int, val gilded: Int, children_count: Int, date_posted: Date)
  extends RedditPost(redditId, author, subreddit, content, ups, downs, children_count, date_posted) {

  override def toString(): String = return s"$subreddit/$parentId/$redditId"
}