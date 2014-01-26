package com.felixmilea.vorbit.analysis

import scala.collection.mutable.ArrayBuffer
import java.util.regex.Pattern
import com.felixmilea.vorbit.utils.Log

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
  val urlPattern = Pattern.compile("(https?://.*?(?=\\s))")
  val E = 'E' // escape character..use of uppercase is okay bc input text is always lowercased first

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

      val urls = new ArrayBuffer[String]

      // match output against the url mattern
      val urlMatcher = urlPattern.matcher(output)

      // parse out urls and store them
      while (urlMatcher.find()) urls += urlMatcher.group(1)

      // replace urls with placeholders
      for (i <- 0 until urls.length) output = output.replaceAllLiterally(urls(i), s"${E}URL${i}${E}")

      // replace special characters with an escaped caps word literal (to avoid collision with phrases)
      for (ex <- exceptions) output = output.replaceAll(ex._1._2, s"$E${ex._2.toUpperCase}$E")

      // replace spaces between phrases with escape char
      for (p <- phrases) output = output.replaceAll(p, p.replaceAll(" ", s"$E"))

      // split string on word breaks and then undo all the escaping
      output.split("\\b").map(n => {
        var output = n

        // replace word literals with symbols
        for (ex <- exceptions) output = output.replaceAll(s"${E}${ex._2.toUpperCase}${E}", ex._1._1)

        // replace URL placeholders back with actual urls
        for (i <- 0 until urls.length) output = output.replaceAllLiterally(s"${E}URL${i}$E", urls(i))

        // replace escape char with space in phrases
        for (p <- phrases) output = output.replaceAll(p, p.replaceAll(s"$E", " "))

        output
      })
    }
  }

  implicit def addNgramMethods(s: String) = new NgramString(s)
}