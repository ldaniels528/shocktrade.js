package com.shocktrade.server.services

import com.shocktrade.util.StringHelper._
import org.scalajs.nodejs.htmlparser2.{HtmlParser2, ParserHandler, ParserOptions}
import org.scalajs.nodejs.{NodeRequire, console, errors}

import scala.concurrent.Promise
import scala.scalajs.js
import scala.scalajs.js.JSON
import scala.scalajs.runtime._
import scala.util.{Failure, Success, Try}

/**
  * HTML Script Parser
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
class ScriptParser[T]()(implicit require: NodeRequire) {
  private val htmlParser = HtmlParser2()

  def parse(html: String, anchor: String) = {
    val promise = Promise[Option[T]]()
    val parser = htmlParser.Parser(new ParserHandler {
      val sb = new StringBuilder()
      val quotes = js.Array[T]()

      override def onclosetag = (tag: String) => {
        if (tag == "script") parseJsonQuote(sb.toString(), anchor) foreach (quotes.push(_))
        sb.clear()
      }

      override def ontext = (text: String) => sb.append(text)

      override def onend = () => promise.success(quotes.headOption)

      override def onerror = (err: errors.Error) => promise.failure(wrapJavaScriptException(err))

    }, new ParserOptions(decodeEntities = true, lowerCaseTags = true))

    parser.write(html)
    parser.end()
    promise.future
  }

  @inline
  private def parseJsonQuote(rawText: String, anchor: String) = {
    for {
      index <- rawText.indexOfOpt(anchor)
      text = rawText.substring(index + anchor.length)
      limit <- findEndOfJsonBlock(text)
      jsonString = text.take(limit)
      quote <- Try(JSON.parse(jsonString).asInstanceOf[T]) match {
        case Success(qss) => Option(qss)
        case Failure(e) =>
          console.error(s"parseScript: Error occurred: ${e.getMessage}")
          None
      }
    } yield quote
  }

  @inline
  private def findEndOfJsonBlock(text: String): Option[Int] = {
    var pos = 0
    var level = 0
    val ca = text.toCharArray
    do {
      ca(pos) match {
        case '{' => level += 1
        case '}' => level -= 1
        case ch =>
      }
      pos += 1
    } while (pos < ca.length && level > 0)

    if (level == 0) Some(pos) else None
  }
  
}
