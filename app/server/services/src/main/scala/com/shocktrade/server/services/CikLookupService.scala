package com.shocktrade.server.services

import com.shocktrade.common.util.StringHelper._
import com.shocktrade.server.services.CikLookupService._
import io.scalajs.nodejs.Error
import io.scalajs.npm.htmlparser2
import io.scalajs.npm.htmlparser2.{ParserHandler, ParserOptions}
import io.scalajs.npm.request.Request
import io.scalajs.util.ScalaJsHelper._

import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.scalajs.js
import scala.scalajs.js.JSConverters._
import scala.scalajs.js.annotation.ScalaJSDefined
import scala.scalajs.runtime._
import scala.util.{Failure, Success}

/**
  * Cik Lookup Service
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
class CikLookupService() {

  def apply(symbol: String)(implicit ec: ExecutionContext): Future[Option[CikLookupResponse]] = {
    val promise = Promise[Option[CikLookupResponse]]()
    val startTime = js.Date.now()
    Request.getFuture(s"https://www.sec.gov/cgi-bin/browse-edgar?CIK=$symbol&action=getcompany") onComplete {
      case Success((response, html)) => parseHtml(symbol, html, startTime, promise)
      case Failure(e) => promise.failure(e)
    }
    promise.future
  }

  private def parseHtml(symbol: String, html: String, startTime: Double, promise: Promise[Option[CikLookupResponse]]) = {
    val quotes = js.Array[CikLookupResponse]()
    var values = js.Dictionary[js.Array[String]]()
    val attributesStack = js.Array[js.Dictionary[String]]()
    val textStack = js.Dictionary[StringBuilder]()
    var currentTag = ""

    val parser = new htmlparser2.Parser(new ParserHandler {
      override def onopentag(tag: String, attributes: js.Dictionary[String]) {
        currentTag = tag
        attributesStack.push(attributes)
      }

      override def onclosetag(tag: String) {
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

      override def ontext(text: String) {
        textStack.getOrElseUpdate(currentTag, new StringBuilder()).append(text.trim)
      }

      override def onend() {
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

      override def onerror(err: Error): Unit = promise.failure(wrapJavaScriptException(err))

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