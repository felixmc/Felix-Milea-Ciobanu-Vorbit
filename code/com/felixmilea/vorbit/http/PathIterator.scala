package com.felixmilea.vorbit.http

class PathIterator(path: Seq[String]) extends Iterator[String] {
  private[this] var index = 0

  def pathState: String = "/" + path.slice(0, index).mkString("/")
  override def toString: String = pathState

  override def hasNext: Boolean = path.length > index
  override def next: String = {
    if (hasNext) {
      val segment = path(index)
      index += 1
      return segment
    } else {
      throw new NoSuchElementException()
    }
  }

}

object PathIterator {
  def apply(path: Seq[String]) = new PathIterator(path)
}