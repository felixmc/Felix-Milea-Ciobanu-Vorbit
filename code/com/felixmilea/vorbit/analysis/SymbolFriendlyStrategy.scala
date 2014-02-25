package com.felixmilea.vorbit.analysis

import TextUnitParserStrategy._

object SymbolFriendlyStrategy extends TextUnitParserStrategy(
  name = "symbolWords",
  normalizations = Vector((" vs." -> " vs "), (" u.s." -> " united states"), ("police man" -> "policeman"), // general word corrections
    ("[\\(\\)\\[\\]]" -> " "), // convert parenthesis to spaces
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
    "(?<=\\s|^)\\d\\)", // numbered list e.g. 1) 2)
    "(?<=\\s)'(?=\\s)", // lonely apostrophes
    "\\\\"), // back slashes
  phrases = Vector("united states"),
  wordSplitExceptions = Vector(
    (W("&") -> "and"),
    (W("-") -> "hyphen"),
    (W("_") -> "underscore"),
    (W("/") -> "slash"),
    (BD("\\$") -> "dollarSign"),
    (D(",") -> "numberComma"),
    (D("\\.") -> "numberPeriod"),
    (AD("%") -> "percent"),
    (W("'") -> "apostrophe")),
  lowercase = true,
  ngramFilter = n => n.replaceAll("[\\s]+", ""))