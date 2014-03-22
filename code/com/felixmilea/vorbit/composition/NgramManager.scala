package com.felixmilea.vorbit.composition

import java.sql.PreparedStatement
import java.sql.ResultSet
import com.felixmilea.vorbit.data.DBConnection
import com.felixmilea.vorbit.data.ResultSetIterator
import com.felixmilea.vorbit.utils.Loggable
import com.felixmilea.vorbit.utils.AppUtils
import scala.collection.mutable.HashMap

class NgramManager private (val n: Int, dataset: Int, subset: Int, edition: Int) extends Loggable {

  if (n < 1)
    throw new IllegalArgumentException(s"Value '$n' for parameter n does not satisfy n > 0")

  private[this] val db = new DBConnection(true)

  val gramResults = db.conn
    .prepareStatement(s"SELECT `id`, `1gram`, `freq` FROM `1grams` WHERE `dataset`=$dataset AND `subset`=$subset AND `edition`=$edition ORDER BY `id`")
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
      data(ids.length + 1).getOrElse(ids.toList.::(first), (0, 0))._1
    }

  def getId(first: Int, ids: Int*): Int =
    if (ids.length + 1 > n) {
      throw new IllegalArgumentException(s"Number of arguments must be less than or equal to $n (n)")
    } else {
      data(ids.length + 1).getOrElse(ids.toList.::(first), (0, 0))._2
    }

  def getSet(nId: Int): Map[List[Int], (Int, Int)] = {
    if (nId < 1 || nId > n)
      throw new IllegalArgumentException(s"Value '$n' for parameter n does not satisfy n > 0 && n <= $n")
    return data(nId)
  }

  private[this] def ngramStatement(n: Int): PreparedStatement = {
    val cols = (1 to n).map("`grams`.`gram" + _ + "`").mkString(",")
    db.conn.prepareStatement(
      s"SELECT $cols, `grams`.`freq`, `grams`.`id`" +
        s" FROM `${n}grams` AS `grams`" +
        " LEFT JOIN `1grams` ON  `grams`.`gram1` = `1grams`.`id`" +
        s" WHERE `1grams`.`dataset` = $dataset AND `1grams`.`subset` = $subset AND `1grams`.`edition` = $edition"
        + " ORDER BY `grams`.`gram1`")
  }

  private[this] val data: Map[Int, Map[List[Int], (Int, Int)]] =
    (1 to n).map(n => {
      n -> (if (n == 1) {
        ResultSetIterator(gramResults).map(r => List(r.getInt(1)) -> (r.getInt("freq"), r.getInt(1))).toMap
      } else {
        val getNgrams = ngramStatement(n)
        val ngrams =
          ResultSetIterator(getNgrams.executeQuery()).map(r => {
            val grams = (1 to n).map(r.getInt(_)).toList
            (grams -> (r.getInt(n + 1), r.getInt(n + 2)))
          }).toMap
        getNgrams.close()
        ngrams
      }).toMap
    }).toMap

  db.conn.close()
}

object NgramManager {
  private val managers = new HashMap[(Int, Int, Int, Int), NgramManager]

  def apply(n: Int, dataset: Int, subset: Int, edition: Int): NgramManager = {

    managers.get((n, dataset, subset, edition)) match {
      case Some(man) => return man
      case None => {
        val man = new NgramManager(n, dataset, subset, edition)
        managers += (n, dataset, subset, edition) -> man
        return man
      }
    }
  }
}