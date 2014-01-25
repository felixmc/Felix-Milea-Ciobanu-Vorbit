package com.felixmilea.vorbit.analysis

class TextUnitParser(val config: TextUnitParserConfig = TextUnitParserConfig.getDefault) {
  import TextUnitParser._

  def parse(inputs: Seq[String]): Seq[Seq[String]] = {
    return inputs.par.map(s => parse(s)).seq
  }

  def parse(input: String): Seq[String] = {
    var ngrams = input
      // lowercase to remove irregularities
      .toLowerCase()

      // normalize input
      .normalize(config.normalizations)

      // fix escaped sequences
      .escape(config.escapedSequences)

      // remove irrelevant characters/phrases
      .removeAll(config.removePatterns)

      // split by word boundaries, with exceptions
      .wordSplit(config.phrases, config.wordSplitExceptions)

      // apply to all ngrams..
      .map(n => n
        // remove whitespace
        .replaceAll("[\\s]+", ""))

    return ngrams.filter(n => !n.isEmpty) // remove empty strings from result set
  }

}

object TextUnitParser {

  def isGoodSource(text: String): Boolean = {
    return text != "[deleted]"
  }

  implicit class NgramString(s: String) {
    def normalize(norms: Seq[(String, String)]): String = {
      var output = s
      for (norm <- norms) output = output.replaceAll(norm._1, norm._2)
      return output
    }

    def escape(escapes: Seq[(String, String)]): String = {
      var output = s
      for (escape <- escapes) output = output.replaceAll(escape._1, escape._2)
      return output
    }

    def removeAll(patterns: Seq[String]): String = {
      var output = s
      for (patt <- patterns) output = output.replaceAll(patt, "")
      return output
    }

    def wordSplit(phrases: Seq[String], exceptions: Seq[((String, String), String)]): Seq[String] = {
      var output = s

      // replace spaces between phrases with _
      for (p <- phrases) output = output.replaceAll(p, p.replaceAll(" ", "_"))

      // replace special characters with a caps word literal (to avoid collision with phrases), escaped with _'s
      for (ex <- exceptions) output = output.replaceAll(ex._1._2, s"_${ex._2.toUpperCase}_")

      output.split("\\b").map(n => {
        var output = n

        // replace word literals with symbols
        for (ex <- exceptions) output = output.replaceAll(s"_${ex._2.toUpperCase}_", ex._1._1)
        output
      })
    }
  }

  implicit def addNgramMethods(s: String) = new NgramString(s)
}