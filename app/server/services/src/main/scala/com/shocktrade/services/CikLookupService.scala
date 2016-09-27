package com.shocktrade.services

import com.shocktrade.services.CikLookupService.CikLookupResponse
import com.shocktrade.util.StringHelper._
import org.scalajs.nodejs.htmlparser2.{HtmlParser2, ParserHandler, ParserOptions}
import org.scalajs.nodejs.request.Request
import org.scalajs.nodejs.util.ScalaJsHelper._
import org.scalajs.nodejs.{NodeRequire, errors}

import scala.concurrent.{ExecutionContext, Promise}
import scala.scalajs.js
import scala.scalajs.js.JSConverters._
import scala.scalajs.js.annotation.ScalaJSDefined
import scala.scalajs.runtime._
import scala.util.{Failure, Success}

/**
  * Cik Lookup Service
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
class CikLookupService()(implicit require: NodeRequire) {
  private val htmlParser = HtmlParser2()
  private val request = Request()

  def apply(symbol: String)(implicit ec: ExecutionContext) = {
    val promise = Promise[Option[CikLookupResponse]]()
    val startTime = js.Date.now()
    request.getFuture(s"https://www.sec.gov/cgi-bin/browse-edgar?CIK=$symbol&action=getcompany") onComplete {
      case Success((response, html)) => parseHtml(symbol, html, startTime, promise)
      case Failure(e) => promise.failure(e)
    }
    promise.future
  }

  private def parseHtml(symbol: String, html: String, startTime: Double, promise: Promise[Option[CikLookupResponse]]) = {
    val parser = htmlParser.Parser(new ParserHandler {
      val quotes = js.Array[CikLookupResponse]()
      var values = js.Dictionary[js.Array[String]]()
      val attributesStack = js.Array[js.Dictionary[String]]()
      val textStack = js.Dictionary[StringBuilder]()
      var currentTag = ""

      override def onopentag = (tag: String, attributes: js.Dictionary[String]) => {
        currentTag = tag
        attributesStack.push(attributes)
      }

      override def onclosetag = (tag: String) => {
        val attributes = attributesStack.pop()
        val text = textStack.remove(tag).map(_.toString()).getOrElse("")
        tag match {
          case "a" =>
            for {
              uri <- attributes.get("href")
              index <- uri.indexOfOpt("?")
              mapping = Map(uri.substring(index + 1).split("&") flatMap (_.split("=") match {
                case Array(k, v) => Some(k -> v)
                case _ => None
              }): _*)
              cikNumber <- mapping.get("CIK")
            } {
              values("CIK") = js.Array(cikNumber)
            }
          case "span" =>
            attributes.get("class").foreach { key =>
              values.getOrElseUpdate(key, js.Array()).append(text)
            }
          case _ =>
        }
      }

      override def ontext = (text: String) => {
        textStack.getOrElseUpdate(currentTag, new StringBuilder()).append(text.trim)
      }

      override def onend = () => {
        promise.success(values.get("CIK") map { cikNumber =>
          new CikLookupResponse(
            symbol = symbol,
            CIK = cikNumber.mkString("\n"),
            companyName = values.get("companyName").map(_.mkString("\n")).orUndefined,
            mailerAddress = values.get("mailerAddress").orUndefined,
            responseTime = js.Date.now() - startTime
          )
        })
      }

      override def onerror = (err: errors.Error) => promise.failure(wrapJavaScriptException(err))

    }, new ParserOptions(decodeEntities = true, lowerCaseTags = true))

    parser.write(html)
    parser.end()
  }

}

/**
  * Cik Lookup Service Companion
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
object CikLookupService {

  @ScalaJSDefined
  class CikLookupResponse(val symbol: String,
                          val CIK: String,
                          val companyName: js.UndefOr[String],
                          val mailerAddress: js.UndefOr[js.Array[String]],
                          val responseTime: js.UndefOr[Double]) extends js.Object

}