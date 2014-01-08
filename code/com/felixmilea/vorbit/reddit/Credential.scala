package com.felixmilea.vorbit.reddit

import scala.io.Source

class Credential(user: String, pass: String) {
  val username = user
  val password = pass
  override def toString(): String = s"${username} : ${password}"
}

object Credential {

  def fromFile(filePath: String): List[Credential] = {
    return Source.fromFile(filePath).getLines.map(line => {
      val parts = line.split(":")
      new Credential(parts(0).trim, parts(1).trim)
    }).toList
  }

}