package com.felixmilea.vorbit.data

import com.felixmilea.vorbit.utils.ConfigManager
import com.felixmilea.vorbit.utils.Log
import java.util.Properties
import java.sql.Connection
import java.sql.Statement
import com.mysql.jdbc.Driver
import java.sql.ResultSet
import java.sql.PreparedStatement
import java.sql.DriverManager
import java.sql.CallableStatement

class DBConnection(autoConnect: Boolean = false) {
  private val protocol = "jdbc:mysql://"

  private var connection: Connection = null
  private var statement: Statement = null
  private var preparedStatement: PreparedStatement = null
  private var resultSet: ResultSet = null

  if (autoConnect) connect()

  def conn = connection

  def connect() {
    val props = new Properties()
    props.setProperty("user", DBConfig("username"))
    props.setProperty("password", DBConfig("password"))

    Class.forName("com.mysql.jdbc.Driver")
    connection = DriverManager.getConnection(s"$protocol${DBConfig("host")}:3306/${DBConfig("database")}", props)

    connection.setAutoCommit(false)
    statement = connection.createStatement
  }

  def executeQuery(query: String): ResultSet = statement.executeQuery(query)
  def execute(query: String): Boolean = statement.execute(query)

}