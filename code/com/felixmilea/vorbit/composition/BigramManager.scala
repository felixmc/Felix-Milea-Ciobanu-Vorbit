package com.felixmilea.vorbit.composition

import com.felixmilea.vorbit.data.DBConnection
import javax.sql.RowSet
import scala.collection.immutable.HashMap
import scala.collection.mutable.MapBuilder

class BigramManager(dataSet: String) {
  private[this] val table = s"mdt_${dataSet}_c1"
  private[this] val db = new DBConnection(true)
  private[this] val query = db.conn.prepareStatement(s"SELECT * FROM `$table` ORDER BY `ngram1`")

  private[this] val data: Map[Int, Map[Int, Int]] = {
    // get rows
    val rowSet = query.executeQuery()

    // builds final immutable hashmap to be accessed through API
    val dataBuilder = new MapBuilder[Int, Map[Int, Int], HashMap[Int, HashMap[Int, Int]]](new HashMap[Int, HashMap[Int, Int]])

    // used to build freq hash map for a specific ngram1
    val freqBuilder = new MapBuilder[Int, Int, HashMap[Int, Int]](emptyFreqMap)

    // start current ngram1 at an invalid id to
    var currentNgram1 = -1

    // passes freqs of current ngram1 to the databuilder and clears the freq builder
    def storeNgram1Freqs() = {
      // if this is not the first iteration (on first iteration it doesn't need to store anything in the dataset)
      if (currentNgram1 != -1) {
        // store this ngram1's frequencies in the final data and clear the hashmap builder
        dataBuilder += (currentNgram1 -> freqBuilder.result)
        freqBuilder.clear
      }
    }

    while (rowSet.next()) {
      val ngram1 = rowSet.getInt("ngram1")
      val ngram2 = rowSet.getInt("ngram2")
      val freq = rowSet.getInt("freq")

      // since we're ordering by ngram1 in our SQL query, we can build one sub hashmap at a time for freqs
      // check to see if the ngram has changed since last row
      if (currentNgram1 != ngram1) {
        // if ngram1 has changed, time to store away the freqs of last ngram1, which also clears the freqBuilder
        storeNgram1Freqs()

        // and update the currentNgram1
        currentNgram1 = ngram1
      }

      // add the ngram2 freq of the current ngram1 to the hashmap builder
      freqBuilder += (ngram2 -> freq)
    }

    // add the freqs of the last ngram to builder
    storeNgram1Freqs()

    // close db connection
    db.conn.close()

    // create immutable hashmap
    dataBuilder.result()
  }

  private def emptyFreqMap = new HashMap[Int, Int]
  private val noFreqs = emptyFreqMap

  // mutable map that holds total freqs for a single ngram1
  private val totalsCache: scala.collection.mutable.Map[Int, Int] = new scala.collection.mutable.HashMap[Int, Int]

  // gets total freqs for a single ngram1 or calculates it on spot and caches it
  def getTotal(id: Int): Int = {
    totalsCache.getOrElse(id, {
      val total = getNgram1(id).values.sum
      totalsCache.put(id, total)
      total
    })
  }

  def getNgram1(id: Int): Map[Int, Int] = data.getOrElse(id, noFreqs)

  // gets the ngram2 from the bigram associated with the given freq for the given ngram1
  def getNgram2(id: Int, freq: Int): Int = {
    var tFreq = 0

    getNgram1(id).find(kv => {
      tFreq = tFreq + kv._2
      tFreq >= freq
    }) match {
      case Some(kv) => kv._1
      case _ => -1
    }
  }

  def getFreq(ngram1Id: Int)(ngram2Id: Int): Int = getNgram1(ngram1Id).getOrElse(ngram2Id, 0)
}