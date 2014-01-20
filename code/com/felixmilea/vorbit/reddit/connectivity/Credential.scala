package com.felixmilea.vorbit.reddit.connectivity

import scala.io.Source

class Credential(val username: String, val password: String) {
  override def toString(): String = s"$username : $password"
}

object Credential {
  def fromFile(filePath: String): List[Credential] = {
    return Source.fromFile(filePath).getLines.map(line => {
      val parts = line.split(":")
      new Credential(parts(0).trim, parts(1).trim)
    }).toList
  }
}