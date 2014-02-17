package com.felixmilea.vorbit.analysis

import TextUnitParserStrategy._

object WordOnlyStrategy extends TextUnitParserStrategy(
  name = "wordOnly",
  normalizations = Vector((" vs." -> " vs "), // general word corrections
    ("[\\(\\)\\[\\]]" -> " "), // convert parenthesis to spaces
    ("\t" -> ""), // remove tabs (necessary for next one)
    ("[\\r\\n]{2,}" -> " "), // two or more whitespace characters as a new line placeholder
    ("[\\r\\n]{3,}" -> "\n") // two or more whitespace characters as a new line placeholder
    ),
  escapedSequences = Vector(("&amp;" -> "&"), ("&gt;" -> ">"), ("&lt;" -> "<")),
  removePatterns = Vector(
    "[\"*]+", // quotes and formatting (bold/italics)
    "~~(.*?)~~", // strikethrough content
    //        "\\(\\w(.)*?\\)", // paranthesis content
    "\\[(.)*?\\]", // bracketed content
    "(?<=\\s|^)\\d\\)", // numbered list e.g. 1) 2)
    "(?<=\\s)'(?=\\s)", // lonely apostrophes
    "\\\\", // back slashes
    "[\\d]+:[\\d]+", // verse numbers
    "[;:'~^#@|\\-=_`{}><]+"),
  wordSplitExceptions = Vector(
    (W("&") -> "and"),
    (W("-") -> "hyphen"),
    (W("_") -> "underscore"),
    (W("/") -> "slash"),
    (BD("\\$") -> "dollarSign"),
    (D(",") -> "numberComma"),
    (AD("%") -> "percent") //    (W("'") -> "apostrophe")
    ),
  lowercase = true,
  ngramFilter = n => n.replaceAll("[\\s]", ""))