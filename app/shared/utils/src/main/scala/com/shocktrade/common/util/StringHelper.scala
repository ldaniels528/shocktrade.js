package com.shocktrade.common.util

/**
 * String Helper
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
object StringHelper {

  case class Accumulator(lines: List[String] = Nil, sb: StringBuilder = new StringBuilder(), inQuotes: Boolean = false, jsLevel: Int = 0) {
    def results: List[String] = (if (sb.nonEmpty) sb.toString() :: lines else lines).reverse
  }

  final implicit class NthExtensions(val number: Int) extends AnyVal {

    @inline
    def nth: String = {
      val suffix = number.toString match {
        case n if n.endsWith("11") | n.endsWith("12") | n.endsWith("13") => "th"
        case n if n.endsWith("1") => "st"
        case n if n.endsWith("2") => "nd"
        case n if n.endsWith("3") => "rd"
        case _ => "th"
      }
      s"$number$suffix"
    }

  }

  /**
   * String Extensions
   * @author Lawrence Daniels <lawrence.daniels@gmail.com>
   */
  final implicit class StringExtensions(val string: String) extends AnyVal {

    @inline
    def extractAll(tok0: String, tok1: String, fromIndex: Int = 0): List[String] = string.findIndices(tok0, tok1, fromIndex) match {
      case Some((p0, p1)) => string.substring(p0 + tok0.length, p1 - tok1.length).trim :: string.extractAll(tok0, tok1, p1)
      case None => Nil
    }

    @inline
    def findIndices(tok0: String, tok1: String): Option[(Int, Int)] = for {
      start <- string.indexOfOpt(tok0)
      end <- string.indexOfOpt(tok1, start)
    } yield (start, end)

    @inline
    def indexOfOpt(s: String): Option[Int] = string.indexOf(s) match {
      case -1 => None
      case index => Some(index)
    }

    @inline
    def indexOfOpt(s: String, fromIndex: Int): Option[Int] = string.indexOf(s, fromIndex) match {
      case -1 => None
      case index => Some(index)
    }

    @inline
    def findIndices(tok0: String, tok1: String, fromIndex: Int): Option[(Int, Int)] = for {
      start <- string.indexOfOpt(tok0, fromIndex)
      end <- string.indexOfOpt(tok1, start)
    } yield (start, end)

    @inline
    def isBlank: Boolean = string.trim.isEmpty

    @inline
    def nonBlank: Boolean = string.trim.nonEmpty

    @inline
    def nonValidEmail: Boolean = !string.isValidEmail

    @inline
    def isValidEmail: Boolean = string.contains("@") // TODO

    @inline
    def limitTo(length: Int): String = if (string.length > length) string.take(length) + "..." else string

    @inline
    def safeSplit(delimiter: Char, limit: Int): List[String] = {
      val accumulator = string.toCharArray.foldLeft(Accumulator()) {
        case (acc@Accumulator(lines, sb, inQuotes, jsLevel), ch) if inQuotes || lines.size >= limit => sb.append(ch); acc
        case (acc@Accumulator(_, sb, _, jsLevel), ch) if ch == '{' => sb.append(ch); acc.copy(jsLevel = jsLevel + 1)
        case (acc@Accumulator(_, sb, _, jsLevel), ch) if ch == '}' => sb.append(ch); acc.copy(jsLevel = jsLevel - 1)
        case (acc@Accumulator(_, sb, inQuotes, _), ch) if ch == '"' => sb.append(ch); acc.copy(inQuotes = !inQuotes)
        case (acc@Accumulator(lines, sb, _, jsLevel), ch) if ch == delimiter && jsLevel == 0 =>
          val newLine = sb.toString()
          sb.clear()
          acc.copy(lines = newLine :: lines)
        case (acc@Accumulator(_, sb, _, _), ch) => sb.append(ch); acc
      }
      accumulator.results
    }

    @inline
    def unquote: String = string match {
      case s if s.startsWith("\"") && s.endsWith("\"") => s.drop(1).dropRight(1)
      case s => s
    }

  }

}
