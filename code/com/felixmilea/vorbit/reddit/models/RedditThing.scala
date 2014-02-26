package com.felixmilea.vorbit.reddit.models

abstract class RedditThing(val redditId: String, val thingType: RedditThing.TypeId.TypeId)

object RedditThing {
  object TypeId extends Enumeration {
    type TypeId = Value
    val t1, t2, t3, t4, t5, t6, t8 = Value
  }
}