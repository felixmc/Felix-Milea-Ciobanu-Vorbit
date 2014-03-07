package com.felixmilea.vorbit.http

import akka.util.ByteString

class RichByteString(raw: ByteString, private var index: Int = 0) {
  def this(s: String) = this(ByteString.fromString(s))

  private[this] def data = if (isEmpty) ByteString.empty else raw.takeRight(raw.length - index)
  def copy(): ByteString = data

  private[this] def incIdx(i: Int) {
    index += i
  }

  def isEmpty: Boolean = index >= raw.length

  def takeUntil(delimiter: ByteString, inclusive: Boolean = false): ByteString = {
    val startIdx = data.indexOfSlice(delimiter)
    val cachedData = data
    val result =
      if (startIdx < 0) {
        incIdx(cachedData.length)
        cachedData
      } else {
        val endIdx = startIdx + delimiter.length
        incIdx(startIdx + delimiter.length)
        cachedData slice (0, if (inclusive) endIdx else startIdx)
      }

    return result
  }

  def toAscii: String = toAscii(true)
  def toAscii(trim: Boolean = true): String = {
    val ascii = data.decodeString("US-ASCII")
    if (trim) ascii.trim
    else ascii
  }

}

object RichByteString {
  def apply(s: String) = new RichByteString(s)
  def apply(raw: ByteString, index: Int = 0) = new RichByteString(raw, index)
  implicit def toRichByteString(bs: ByteString) = new RichByteString(bs)
  implicit def fromRichByteString(bs: RichByteString) = bs.copy
}