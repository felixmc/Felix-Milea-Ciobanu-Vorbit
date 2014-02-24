package com.felixmilea.vorbit.actors

import akka.actor.ActorSelection
import com.felixmilea.vorbit.data.DBConnection
import com.felixmilea.vorbit.data.ResultSetIterator
import com.felixmilea.vorbit.utils.AppUtils

class RedditCorpusRetriever extends ManagedActor {
  import RedditCorpusRetriever._

  private[this] lazy val db = {
    val dtb = new DBConnection(true)
    dtb.conn.setAutoCommit(true)
    dtb
  }
  private[this] lazy val getPosts = db.conn.prepareStatement("SELECT `reddit_id`, `type`, `title`, `content` FROM `reddit_corpus` WHERE `dataset`=? AND `subset`=?")
  private[this] lazy val getChildren = db.conn.prepareStatement("SELECT `reddit_id`, `content` FROM `reddit_corpus` WHERE `dataset`=? AND `subset`=? AND `parent`=?")

  // TODO: fix child subset hack
  private[this] lazy val childSubset = AppUtils.config.persistence.data.subsets("children")

  def doReceive = {
    case Request(item, receiver) => item match {
      case Posts(dataset, subset, individual) => {
        getPosts.setInt(1, dataset)
        getPosts.setInt(2, subset)
        val results = getPosts.executeQuery()
        val posts = ResultSetIterator(results).map(r => {
          val rType = r.getString(2)
          Post(dataset, subset, rType + "_" + r.getString(1), if (rType == "t3") r.getString(3) else r.getString(4))
        }).toList

        results.close()

        if (individual) {
          posts.foreach { receiver ! _ }
        } else {
          receiver ! posts
        }
      }
      case Children(dataset, parentSubset, parent, individual) => {
        getChildren.setInt(1, dataset)
        getChildren.setInt(2, childSubset)
        getChildren.setString(3, parent)
        val results = getChildren.executeQuery()
        val children = ResultSetIterator(results).map(r => {
          ChildPost(dataset, childSubset, parent, "t1_" + r.getString(1), r.getString(2))
        }).toList

        results.close()

        if (individual) {
          children.foreach { receiver ! _ }
        } else {
          receiver ! children
        }
      }
    }
  }

}

object RedditCorpusRetriever {
  trait RedditItem
  case class Posts(dataset: Int, subset: Int, individual: Boolean = true) extends RedditItem
  case class Children(dataset: Int, subset: Int, parent: String, individual: Boolean = true) extends RedditItem

  case class Request(item: RedditItem, receiver: ActorSelection)

  trait Result
  case class Post(dataset: Int, subset: Int, redditId: String, content: String) extends Result
  case class ChildPost(dataset: Int, subset: Int, parent: String, redditId: String, content: String) extends Result
}