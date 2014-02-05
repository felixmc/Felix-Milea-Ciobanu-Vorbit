package com.felixmilea.vorbit.composition

import com.felixmilea.vorbit.data.DBConnection
import scala.collection.mutable.HashMap
import com.felixmilea.vorbit.utils.Loggable

class TextUnitInjector(dataSet: String) extends Loggable {
  private[this] val table = s"mdt_${dataSet}_b1"
  private[this] val db = new DBConnection(true)
  private[this] val getTextUnitQuery = db.conn.prepareStatement(s"SELECT `ngram` FROM `$table` WHERE `id` = ? LIMIT 1")

  private val cache = new HashMap[Int, String]

  def parseChain(chain: Seq[Int]): Seq[String] = chain.map(parseTextUnit)

  def parseTextUnit(id: Int): String =
    cache.getOrElse(id,
      try {
        getTextUnitQuery.setInt(1, id)
        val rowSet = getTextUnitQuery.executeQuery()
        val unit = if (rowSet.next()) rowSet.getString("ngram") else null
        rowSet.close()
        cache.put(id, unit)
        unit
      } catch {
        case t: Throwable => {
          Error(t.getMessage)
          null
        }
      })

}