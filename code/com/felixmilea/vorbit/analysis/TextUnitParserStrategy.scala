package com.felixmilea.vorbit.analysis

class TextUnitParserStrategy(
  val name: String,
  val normalizations: Seq[(String, String)] = Vector(),
  val escapedSequences: Seq[(String, String)] = Vector(),
  val removePatterns: Seq[String] = Vector(),
  val phrases: Seq[String] = Vector(),
  val wordSplitExceptions: Seq[((String, String), String)] = Vector(),
  val lowercase: Boolean = false,
  val ngramFilter: (String) => String = n => n)

object TextUnitParserStrategy {
  abstract class PatternWrapper(wrapper: String => (String, String)) {
    final def apply(s: String): (String, String) = wrapper(s)
  }

  object W extends PatternWrapper(s => (s, "(?<=\\w)" + s + "(?=\\w)"))
  object BW extends PatternWrapper(s => (s, s + "(?=\\w)"))
  object AW extends PatternWrapper(s => (s, "(?<=\\w)" + s))
  object D extends PatternWrapper(s => (s, "(?<=\\d)" + s + "(?=\\d)"))
  object BD extends PatternWrapper(s => (s, s + "(?=\\d)"))
  object AD extends PatternWrapper(s => (s, "(?<=\\d)" + s))
}