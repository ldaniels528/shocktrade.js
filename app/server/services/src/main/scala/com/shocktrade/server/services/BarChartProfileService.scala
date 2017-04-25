package com.shocktrade.server.services

import com.shocktrade.server.services.BarChartProfileService._
import io.scalajs.nodejs.Error
import io.scalajs.npm.htmlparser2.{Parser, ParserHandler, ParserOptions}
import io.scalajs.npm.request.{Request, RequestBody}

import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.scalajs.js
import scala.scalajs.js.JSConverters._
import scala.scalajs.runtime._

/**
  * Bar Chart Company Profile Service
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
class BarChartProfileService() {

  /**
    * Returns the promise of an option of a Bar Chart company profile for the given symbol
    * @param symbol the given symbol (e.g. "AAPL")
    * @return the promise of an option of a [[BarChartProfile Bar Chart profile]]
    */
  def apply(symbol: String)(implicit ec: ExecutionContext): Future[Option[BarChartProfile]] = {
    val startTime = js.Date.now()
    for {
      (response, html) <- Request.getFuture(toURL(symbol))
      profileOpt <- parseHtml(symbol, html.toString, startTime)
    } yield profileOpt
  }

  /**
    * Parsing the HTML into the option of a Bar Chart company profile
    * @param symbol    the given symbol (e.g. "AAPL")
    * @param html      the given HTML document
    * @param startTime the given service execution start time
    * @return the option of a [[BarChartProfile Bar Chart profile]]
    */
  private def parseHtml(symbol: String, html: RequestBody, startTime: Double)(implicit ec: ExecutionContext) = {
    val promise = Promise[Option[BarChartProfile]]()
    val tagStack = js.Array[Tag]()
    val mappings = js.Dictionary[String]()
    var key: String = null
    val parser = new Parser(new ParserHandler {
      override def onopentag(name: String, attributes: js.Dictionary[String]) = tagStack.push(Tag(name, attributes))

      override def onclosetag(name: String) {
        val tag = tagStack.pop()
        tagPath match {
          case "html.body.td.div.table.tr.td" if name == "strong" => key = tag.text.toString().trim
          case "html.body.td.div.table.tr.td" if name == "a" => setValue(key, tag)
          case "html.body.td.div.table.tr" if tag.isClass("qb_line") && tag.text.nonEmpty => setValue(key, tag)
          case _ =>
        }
      }

      override def ontext(text: String): Unit = tagStack.lastOption foreach (_.text.append(text))

      override def onend() {
        //console.log("mappings => %s", JSON.dynamic.stringify(mappings, null, 4).asInstanceOf[String])
        val quote = for {
          exchange <- mappings.get("Exchange:")
          description <- mappings.get("Description:")
          contactInfo <- mappings.get("Contact Info:").map(_.split("[\n]").map(_.trim).toJSArray)
          ceoPresident <- mappings.get("CEO / President:")
          industrySector <- mappings.get("Industry/Sector:").map(_.split("[,]").map(_.trim).filter(_.nonEmpty).toJSArray)
        } yield {
          new BarChartProfile(
            symbol = symbol,
            exchange = exchange,
            contactInfo = contactInfo,
            ceoPresident = ceoPresident,
            description = description,
            industrySector = industrySector,
            responseTime = js.Date.now() - startTime)
        }
        promise.success(quote)
      }

      override def onerror(err: Error): Unit = promise.failure(wrapJavaScriptException(err))

      private def tagPath = tagStack.map(_.name).mkString(".")

      private def setValue(key: String, tag: Tag) = {
        //console.log(s"$key [value] $tagPath:${tag.name} - ${tag.text.toString()}")
        mappings.get(key) match {
          case Some(value) => mappings(key) = value + ", " + tag.text.toString().trim
          case None => mappings(key) = tag.text.toString().trim
        }
      }

    }, new ParserOptions(decodeEntities = true, lowerCaseTags = true))

    parser.write(html.toString)
    parser.end()
    promise.future
  }

  @inline
  private def toURL(symbol: String) = s"http://www.barchart.com/profile/stocks/$symbol"

}

/**
  * Bar Chart Company Profile Service Companion
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
object BarChartProfileService {

  /**
    * Represents an HTML tag
    * @param name       the name of the tag (e.g. "table")
    * @param attributes the tag element's attributes
    * @param text       the tag's text
    */
  case class Tag(name: String, attributes: js.Dictionary[String], text: StringBuilder = new StringBuilder()) {

    def isClass(className: String): Boolean = attributes.get("class").contains(className)

  }

  /**
    * Represents a Bar Chart company profile
    */
  class BarChartProfile(val symbol: String,
                        val exchange: js.UndefOr[String],
                        val contactInfo: js.UndefOr[js.Array[String]],
                        val ceoPresident: js.UndefOr[String],
                        val description: js.UndefOr[String],
                        val industrySector: js.UndefOr[js.Array[String]],
                        val responseTime: Double) extends js.Object

}
