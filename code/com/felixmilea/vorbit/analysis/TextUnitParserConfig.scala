package com.felixmilea.vorbit.analysis

class TextUnitParserConfig(
  val normalizations: Seq[(String, String)] = Vector(),
  val escapedSequences: Seq[(String, String)] = Vector(),
  val removePatterns: Seq[String] = Vector(),
  val phrases: Seq[String] = Vector(),
  val wordSplitExceptions: Seq[((String, String), String)] = Vector()) {}

object TextUnitParserConfig {

  def getDefault(): TextUnitParserConfig = {
    return new TextUnitParserConfig(
      normalizations = Vector((" vs." -> " vs "), (" u.s." -> " united states"), ("police man" -> "policeman"), // general word corrections
        ("[\\(\\)]" -> " "), // convert parenthesis to spaces
        ("\t" -> ""), // remove tabs (necessary for next one)
        ("\\s{2,}" -> "\n") // two or more whitespace characters as a new line placeholder
        ),
      escapedSequences = Vector(("&amp;" -> "&"), ("&gt;" -> ">"), ("&lt;" -> "<")),
      removePatterns = Vector(
        "[\"*]+", // quotes and formatting (bold/italics)
        "~~(.*?)~~", // strikethrough content
        //        "\\(\\w(.)*?\\)", // paranthesis content
        "\\[(.)*?\\]", // bracketed content
        ">.*?\n", // quoted content
        "(?<=\\s|^)\\d\\)" // numbered list e.g. 1) 2)
        ),
      phrases = Vector("united states"),
      wordSplitExceptions = Vector(
        (W("&") -> "and"),
        (W("-") -> "hyphen"),
        (W("_") -> "underscore"),
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