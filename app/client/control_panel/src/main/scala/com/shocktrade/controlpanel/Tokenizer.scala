package com.shocktrade.controlpanel

import com.shocktrade.controlpanel.Tokenizer.TokenIterator
import org.scalajs.sjs.OptionHelper._

/**
  * Tokenizer
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
class Tokenizer() {

  def parse(text: String) = new TokenIterator(text)

  def parseFully(text: String) = {
    var tokens: List[String] = Nil
    val c = new TokenIterator(text)
    while (c.hasNext) tokens = c.next() :: tokens
    tokens.reverse
  }

}

/**
  * Tokenizer Companion
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
object Tokenizer {

  type Token = String

  /**
    * Token Iterator
    * @param input the given [[String input text]]
    */
  class TokenIterator(input: String) extends Iterator[Token] {
    private val ca = input.toCharArray
    private var pos = 0

    def hasNext = {
      while (notEOF && ca(pos).isWhitespace) pos += 1
      pos < ca.length
    }

    def next() = (alphaNumeric() ?? numeric() ?? quotes()) getOrElse character()

    def peek = {
      val index = pos
      val result = next()
      pos = index
      result
    }

    def alphaNumeric() = {
      if (hasNext && ca(pos).isLetter) {
        val start = pos
        while (notEOF && ca(pos).isLetterOrDigit) pos += 1
        extract(start)
      } else None
    }

    def character() = {
      if (hasNext) {
        val start = pos
        pos += 1
        String.valueOf(ca(start))
      }
      else throw new IllegalStateException("End of stream")
    }

    def numeric() = {
      if (hasNext && ca(pos).isDigit) {
        val start = pos
        while (notEOF && ca(pos).isDigit) pos += 1
        extract(start)
      } else None
    }

    def quotes() = quoteBackTicks() ?? quoteDouble() ?? quotesSingle()

    def quoteBackTicks() = sequence('`', '`')

    def quoteDouble() = sequence('"', '"')

    def quotesSingle() = sequence('\'', '\'')

    def reset(newPosition: Int) = pos = newPosition

    def sequence(begin: Char, end: Char) = {
      if (hasNext && ca(pos) == begin) {
        val start = pos
        pos += 1
        while (notEOF && ca(pos) != end) pos += 1
        pos += 1
        extract(start)
      }
      else None
    }

    private def notEOF = pos < ca.length

    private def extract(start: Int) = Some(input.substring(start, pos))

  }

  /**
    * Token Enrichment
    * @param token the givne [[Token token]]
    */
  implicit class TokenEnrichment(val token: Token) extends AnyVal {

    @inline
    def isConstant = isNull || isNumeric || isQuoted

    @inline
    def isNull = token == "null"

    @inline
    def isNumeric = token.matches("^[-+]?\\d+(\\.\\d+)?$")

    @inline
    def isOperator = token match {
      case "+" | "-" | "*" | "/" | "%" => true
      case _ => false
    }

    @inline
    def isQuoted = isBackTickQuoted || isDoubleQuoted || isSingleQuoted

    @inline
    def isBackTickQuoted = token.startsWith("`") && token.endsWith("`")

    @inline
    def isDoubleQuoted = token.startsWith("\"") && token.endsWith("\"")

    @inline
    def isSingleQuoted = token.startsWith("'") && token.endsWith("'")

  }

}