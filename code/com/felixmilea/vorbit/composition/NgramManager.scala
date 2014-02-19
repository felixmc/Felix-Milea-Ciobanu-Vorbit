package com.felixmilea.vorbit.composition

import java.sql.PreparedStatement
import java.sql.ResultSet
import com.felixmilea.vorbit.data.DBConnection
import com.felixmilea.vorbit.data.ResultSetIterator
import com.felixmilea.vorbit.utils.Loggable
import com.felixmilea.vorbit.utils.AppUtils

class NgramManager(val n: Int, dataset: String, subset: String, edition: String) extends Loggable {

  if (n < 1)
    throw new IllegalArgumentException(s"Value '$n' for parameter n does not satisfy n > 0")

  private[this] val db = new DBConnection(true)

  val (datasetId, subsetId, editionId) = (AppUtils.config.persistence.data.datasets(dataset),
    AppUtils.config.persistence.data.subsets(subset), AppUtils.config.persistence.data.editions(edition))

  val gramResults = db.conn
    .prepareStatement(s"SELECT `id`, `1gram`, `freq` FROM `1grams` WHERE `dataset`=$datasetId AND `subset`=$subsetId AND `edition`=$editionId ORDER BY `id`")
    .executeQuery()

  val (textUnits, nullId) = {
    var nullId = -1

    def parse(r: ResultSet): (Int, String) = {
      val unit = r.getInt(1) -> r.getString(2)
      if (nullId == -1 && unit._2 == "NULL")
        nullId = unit._1

      return unit
    }

    (ResultSetIterator(gramResults).map(parse).toMap, nullId)
  }

  def freq(first: Int, ids: Int*): Int =
    if (ids.length + 1 > n) {
      throw new IllegalArgumentException(s"Number of arguments must be less than or equal to $n (n)")
    } else {
      freqs(ids.length + 1).getOrElse(ids.toList.::(first), 0)
    }

  def getSet(nId: Int): Map[List[Int], Int] = {
    if (nId < 1 || nId > n)
      throw new IllegalArgumentException(s"Value '$n' for parameter n does not satisfy n > 0 && n <= $n")
    return freqs(nId)
  }

  private[this] def ngramStatement(n: Int): PreparedStatement = {
    val cols = (1 to n).map("`grams`.`gram" + _ + "`").mkString(",")
    db.conn.prepareStatement(
      s"SELECT $cols, `grams`.`freq`" +
        s" FROM `${n}grams` AS `grams`" +
        " LEFT JOIN `1grams` ON  `grams`.`gram1` = `1grams`.`id`" +
        s" WHERE `1grams`.`dataset` = $datasetId AND `1grams`.`subset` = $subsetId AND `1grams`.`edition` = $editionId"
        + " ORDER BY `grams`.`gram1`")
  }

  private[this] val freqs: Map[Int, Map[List[Int], Int]] = (1 to n).map(n => {
    n -> (if (n == 1) {
      ResultSetIterator(gramResults).map(r => List(r.getInt(1)) -> r.getInt("freq")).toMap
    } else {
      val getNgrams = ngramStatement(n)
      val ngrams =
        ResultSetIterator(getNgrams.executeQuery()).map(r => {
          (1 to n).map(r.getInt(_)).toList -> r.getInt(n + 1)
        }).toMap
      getNgrams.close()
      ngrams
    })
  }).toMap

  db.conn.close()
}