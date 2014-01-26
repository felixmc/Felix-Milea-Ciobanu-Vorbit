package com.felixmilea.vorbit.analysis

class TextUnitParserConfig(
  val normalizations: Seq[(String, String)] = Vector(),
  val escapedSequences: Seq[(String, String)] = Vector(),
  val removePatterns: Seq[String] = Vector("[_]+"),
  val phrases: Seq[String] = Vector(),
  val wordSplitExceptions: Seq[((String, String), String)] = Vector()) {}

object TextUnitParserConfig {

  def getDefault(): TextUnitParserConfig = {
    return new TextUnitParserConfig(
      normalizations = Vector((" vs." -> " vs "), (" u.s." -> " united states"), ("police man" -> "policeman"), // general word corrections
        ("' " -> " "), //apostrophes at the end of things or by themselves
        ("\t" -> ""), // remove tabs (necessary for next one)
        ("\\s{2,}" -> "NEWLINE") // two or more whitespace characters as a new line
        ),
      escapedSequences = Vector(("&amp;" -> "&"), ("&gt;" -> ">"), ("&lt;" -> "<")),
      removePatterns = Vector(
        "[\"*_]+", // quotes, formatting, and underscores
        "(~~(.)*?~~)", // strikethrough content
        "[~]{2}", // random double tildas
        "\\(\\w(.)*?\\)", // paranthesis content
        "\\[(.)*?\\]", // bracketed content
        ">(.)*?\\s{2}", // quoted content
        "(?<=\\s|^)\\d\\)" // numbered list e.g. 1) 2)
        ),
      phrases = Vector("united states"),
      wordSplitExceptions = Vector(
        (W("&") -> "and"),
        (W("-") -> "hyphen"),
        (BW("/") -> "slash"),
        (BD("\\$") -> "dollarSign"),
        (D(",") -> "numberComma"),
        (W("'") -> "apostrophe")))
  }

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