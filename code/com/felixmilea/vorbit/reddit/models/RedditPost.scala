package com.felixmilea.vorbit.reddit.models

import java.util.Date

abstract class RedditPost(redditId: String, val author: String, val subreddit: String,
  val content: String, val ups: Int, val downs: Int, val children_count: Int, val date_posted: Date)
  extends RedditThing(redditId) {

}