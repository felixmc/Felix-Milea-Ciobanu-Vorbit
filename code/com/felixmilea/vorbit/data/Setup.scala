package com.felixmilea.vorbit.data

import com.felixmilea.vorbit.utils.Initable

object Setup extends Initable {

  def init() {
    val conn = new DBConnection()
    conn.connect

    val rows = conn.executeQuery("SELECT * FROM `reddit_accounts`")

    while (rows.next()) {
      for (i <- 0 until 8) {

      }
    }

  }

}