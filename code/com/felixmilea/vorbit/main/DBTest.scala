package com.felixmilea.vorbit.main

import com.felixmilea.vorbit.data.DBConnection

object DBTest extends App {
  private[this] lazy val db = new DBConnection(true)
  private[this] lazy val insertAndSelectStatement = db.conn.prepareCall("SET @ngram = ?;INSERT IGNORE INTO `mdt_answerbot_b1`(`ngram`) VALUES (@ngram);SELECT * FROM `mdt_answerbot_b1` WHERE `ngram` = @ngram LIMIT 1")

  insertAndSelectStatement.setString(1, "ngram")
  val rows = insertAndSelectStatement.executeQuery()
  db.conn.commit()

  rows.next()

  println(s"returned id: " + rows.getInt("id"))

}