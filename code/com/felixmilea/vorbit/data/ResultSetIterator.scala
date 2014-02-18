package com.felixmilea.vorbit.data

import java.sql.ResultSet

class ResultSetIterator(results: ResultSet) extends Iterator[ResultSet] {
  results.beforeFirst()
  private[this] var retrievedNext = false
  private[this] var hasMore = results.next()

  override def hasNext: Boolean = {
    if (retrievedNext) {
      hasMore = results.next
      retrievedNext = false
    }
    return hasMore
  }

  override def next: ResultSet = {
    if (retrievedNext)
      hasNext

    if (hasMore) {
      retrievedNext = true
      return results
    } else {
      throw new NoSuchElementException()
    }
  }

}

object ResultSetIterator {
  def apply(results: ResultSet): ResultSetIterator = new ResultSetIterator(results)
}