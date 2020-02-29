package com.shocktrade.common.util

/**
  * String Helper
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
object StringHelper {

  implicit class NthExtensions(val number: Int) extends AnyVal {

    @inline
    def nth = {
      val ending = number.toString match {
        case s if s.endsWith("11") => "th"
        case s if s.endsWith("1") => "st"
        case s if s.endsWith("2") => "nd"
        case s if s.endsWith("3") => "rd"
        case _ => "th"
      }
      s"$number$ending"
    }

  }

  /**
    * String Extensions
    * @author Lawrence Daniels <lawrence.daniels@gmail.com>
    */
  implicit class StringExtensions(val string: String) extends AnyVal {

    @inline
    def extractAll(tok0: String, tok1: String, fromIndex: Int = 0): List[String] = string.findIndices(tok0, tok1, fromIndex) match {
      case Some((p0, p1)) => string.substring(p0 + tok0.length, p1 - tok1.length).trim :: string.extractAll(tok0, tok1, p1)
      case None => Nil
    }

    @inline
    def findIndices(tok0: String, tok1: String) = for {
      start <- string.indexOfOpt(tok0)
      end <- string.indexOfOpt(tok1, start)
    } yield (start, end)

    @inline
    def findIndices(tok0: String, tok1: String, fromIndex: Int) = for {
      start <- string.indexOfOpt(tok0, fromIndex)
      end <- string.indexOfOpt(tok1, start)
    } yield (start, end)

    @inline
    def indexOfOpt(s: String) = string.indexOf(s) match {
      case -1 => None
      case index => Some(index)
    }

    @inline
    def indexOfOpt(s: String, fromIndex: Int) = string.indexOf(s, fromIndex) match {
      case -1 => None
      case index => Some(index)
    }

    @inline
    def isBlank = string.trim.isEmpty

    @inline
    def nonBlank = string.trim.nonEmpty

    @inline
    def isValidEmail = string.contains("@") // TODO

    @inline
    def nonValidEmail = !string.isValidEmail

    @inline
    def limitTo(length: Int) = if(string.length > length) string.take(length) + "..." else string

    @inline
    def unquote = string match {
      case s if s.startsWith("\"") && s.endsWith("\"") => s.drop(1).dropRight(1)
      case s => s
    }

  }

}
