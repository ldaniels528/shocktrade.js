package com.shocktrade.server.services

import com.shocktrade.server.common.LoggerFactory
import com.shocktrade.server.services.EodDataSecuritiesService._
import io.scalajs.nodejs.Error
import io.scalajs.npm.htmlparser2
import io.scalajs.npm.htmlparser2.{ParserHandler, ParserOptions}
import io.scalajs.npm.request.Request
import io.scalajs.util.ScalaJsHelper._

import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.scalajs.js
import scala.scalajs.js.JSConverters._
import scala.scalajs.runtime._
import scala.util.{Failure, Success, Try}

/**
  * Eod Data Securities Service
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
class EodDataSecuritiesService() {
  private val logger = LoggerFactory.getLogger(getClass)

  /**
    * Retrieves a list of securities with limited stock quotes
    * @param exchange    the given exchange (e.g. "OTCBB")
    * @param firstLetter the given first letter (e.g. "A")
    * @return the promise of a collection of [[EodDataSecurity securities]]
    */
  def apply(exchange: String, firstLetter: Char)(implicit ec: ExecutionContext): Future[Seq[EodDataSecurity]] = {
    val promise = Promise[Seq[EodDataSecurity]]()
    Request.getFuture(s"http://eoddata.com/stocklist/$exchange/$firstLetter.htm") onComplete {
      case Success((response, html)) =>
        val parser = new htmlparser2.Parser(new ParserHandler {
          val quotes: js.Array[EodDataSecurity] = js.Array[EodDataSecurity]()
          val attributesStack: js.Array[js.Dictionary[String]] = js.Array[js.Dictionary[String]]()
          val textStack: js.Dictionary[StringBuilder] = js.Dictionary[StringBuilder]()
          var currentTag = ""
          var inTable = false
          var done = false
          val columns: js.Array[String] = js.Array[String]()
          val headers: js.Array[String] = js.Array[String]()
          var skip = 0

          override def onopentag(tag: String, attributes: js.Dictionary[String]) {
            currentTag = tag
            attributesStack.push(attributes)
            if (!inTable && tag == "table" && attributes.get("class").contains("quotes")) inTable = true
          }

          override def onclosetag(tag: String) {
            val attributes = attributesStack.pop()
            val text = textStack.remove(tag).map(_.toString()).getOrElse("")
            if (skip > 0) skip -= 1
            else if (!done && inTable) {
              tag match {
                case "a" if columns.isEmpty => columns.append(text); skip = 1
                case "table" => inTable = false; done = true
                case "td" if columns.nonEmpty => columns.append(text)
                case "th" =>
                  attributes.get("colspan").map(_.toInt) match {
                    case Some(colSpan) => headers.append((1 to colSpan) map (text + _): _*)
                    case None => headers.append(text)
                  }
                case "tr" =>
                  if (columns.nonEmpty) quotes.push(toQuote(exchange, headers, columns))
                  columns.removeAll()
                case _ =>
              }
            }
          }

          override def ontext(text: String): Unit = textStack.getOrElseUpdate(currentTag, new StringBuilder()).append(text.trim)

          override def onend(): Unit = promise.success(quotes)

          override def onerror(err: Error): Unit = promise.failure(wrapJavaScriptException(err))

        }, new ParserOptions(decodeEntities = true, lowerCaseTags = true))

        parser.write(html.toString)
        parser.end()
      case Failure(e) => promise.failure(e)
    }
    promise.future
  }

  private def toQuote(exchange: String, headers: js.Array[String], columns: js.Array[String]): EodDataSecurity = {
    val mapping = js.Dictionary(headers zip columns: _*)
    val symbol = mapping.get("Code").orUndefined
    new EodDataSecurity(
      symbol = symbol, //if(exchange == "OTCBB") symbol.map(_ + ".OB") else symbol,
      exchange = exchange,
      name = mapping.get("Name").orUndefined,
      high = mapping.get("High").flatMap(toDouble("high", _)).orUndefined,
      low = mapping.get("Low").flatMap(toDouble("low", _)).orUndefined,
      close = mapping.get("Close").flatMap(toDouble("close", _)).orUndefined,
      volume = mapping.get("Volume").flatMap(toDouble("volume", _)).orUndefined,
      change = mapping.get("Change1").flatMap(toDouble("change", _)).orUndefined,
      changePct = mapping.get("Change2").flatMap(toDouble("changePct", _)).orUndefined
    )
  }

  @inline
  private def toDouble(fieldName: String, value: String): Option[Double] = {
    if (value.isEmpty) None
    else {
      Try(value.filter(c => c == '.' || c.isDigit).toDouble) match {
        case Success(v) => Option(v)
        case Failure(e) =>
          logger.error(s"$fieldName: ${e.getMessage}")
          None
      }
    }
  }

}

/**
  * Eod Data Securities Service Companion
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
object EodDataSecuritiesService {

  class EodDataSecurity(val symbol: js.UndefOr[String],
                        val exchange: js.UndefOr[String],
                        val name: js.UndefOr[String],
                        val high: js.UndefOr[Double],
                        val low: js.UndefOr[Double],
                        val close: js.UndefOr[Double],
                        val volume: js.UndefOr[Double],
                        val change: js.UndefOr[Double],
                        val changePct: js.UndefOr[Double]) extends js.Object

}
