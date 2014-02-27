package com.felixmilea.vorbit.actors

import com.felixmilea.vorbit.data.DBConnection
import scala.collection.mutable.HashMap

class SubsetNgramAnalyzer extends ManagedActor {
  import SubsetNgramAnalyzer._

  private[this] lazy val db = {
    {
      val dtb = new DBConnection(true)
      dtb.conn.setAutoCommit(true)
      dtb
    }
  }
  private[this] lazy val compareCall = db.conn.prepareCall("{CALL record_ngram_comparison(?,?,?,?,?,?,?)}")
  private[this] lazy val ngramCall = {
    val call = db.conn.prepareCall("{CALL get_ngram_id(?,?,?)}")
    call.registerOutParameter(3, java.sql.Types.INTEGER)
    call
  }

  private[this] val ngramCache = new HashMap[Seq[Int], Int]

  private[this] def getId(ngrams: Seq[Int]): Int = {
    ngramCache.get(ngrams) match {
      case Some(id) => id
      case None => {
        ngramCall.setInt(1, ngrams.size)
        ngramCall.setString(2, genCondition(ngrams))
        ngramCall.execute()
        val id = ngramCall.getInt(3)
        ngramCache.put(ngrams, id)
        return id
      }
    }
  }

  private[this] def genCondition(ngrams: Seq[Int]): String = {
    (1 to ngrams.length).map(i => s"`gram${i}`").mkString("(", ",", ")") + "=" + ngrams.mkString("(", ",", ")")
  }

  private[this] def record(dataset: Int, edition: Int, n: Int, sub1: Int, sub2: Int, ngram1: Int, ngram2: Int) {
    compareCall.setInt(1, dataset)
    compareCall.setInt(2, edition)
    compareCall.setInt(3, n)
    compareCall.setInt(4, sub1)
    compareCall.setInt(5, sub2)
    compareCall.setInt(6, ngram1)
    compareCall.setInt(7, ngram2)
    compareCall.execute()
  }

  def doReceive = {
    case ParentChild(dataset, edition, parent, child) => {

      // for n == 1
      parent._2.foreach(pGram => {
        child._2.foreach(cGram => {
          record(dataset, edition, 1, parent._1, child._1, pGram, cGram)
        })
      })

      Debug("done analyzing comment")

      // for n > 1
      //      for (n <- minLevel to maxLevel) {
      //        seqIterator(n, parent._2)(pGram => {
      //          seqIterator(n, child._2)(cGram => {
      //            record(dataset, edition, n, parent._1, child._1, getId(pGram), getId(cGram))
      //          })
      //        })
      //      }
    }
  }

  def seqIterator(n: Int, ngrams: Seq[Int])(action: Function[Seq[Int], Unit]) {
    for (i <- 0 until ngrams.length) {
      if (i + n <= ngrams.length) {
        action(ngrams.slice(i, i + n))
      }
    }
  }

}

object SubsetNgramAnalyzer {
  val minLevel = 2
  val maxLevel = 2

  case class ParentChild(dataset: Int, edition: Int, parent: (Int, Seq[Int]), child: (Int, Seq[Int]))

}