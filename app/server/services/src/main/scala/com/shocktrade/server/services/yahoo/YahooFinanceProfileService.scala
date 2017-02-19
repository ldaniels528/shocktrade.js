package com.shocktrade.server.services.yahoo

import com.shocktrade.server.services.yahoo.YahooFinanceProfileService._
import io.scalajs.nodejs.Error
import io.scalajs.npm.htmlparser2
import io.scalajs.npm.htmlparser2.{ParserHandler, ParserOptions}
import io.scalajs.npm.request.Request
import io.scalajs.util.PromiseHelper.Implicits._

import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.scalajs.js
import scala.scalajs.js.JSConverters._
import scala.scalajs.js.annotation.ScalaJSDefined
import scala.scalajs.runtime._

/**
  * Yahoo! Finance Profile Service
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
class YahooFinanceProfileService() {

  def apply(symbol: String)(implicit ec: ExecutionContext): Future[Option[YFProfile]] = {
    val startTime = js.Date.now()
    for {
      (response, html) <- Request.getFuture(s"https://finance.yahoo.com/quote/$symbol/profile")
      profile_? <- parseHtml(symbol, html, startTime)
    } yield profile_?
  }

  /**
    * Parsing the HTML into the option of a Bar Chart company profile
    * @param symbol    the given symbol (e.g. "AAPL")
    * @param html      the given HTML document
    * @param startTime the given service execution start time
    * @return the option of a [[YFProfile Bar Chart profile]]
    */
  private def parseHtml(symbol: String, html: String, startTime: Double) = {
    val promise = Promise[Option[YFProfile]]()
    val parser = new htmlparser2.Parser(new ParserHandler {
      val tagStack = js.Array[Tag]()
      val mappings = js.Dictionary[String]()
      var key: String = _

      override def onopentag(name: String, attributes: js.Dictionary[String]) = tagStack.push(Tag(name, attributes))

      override def onclosetag(name: String) = {
        val tag = tagStack.pop()
        tagPath match {
          case "html.body.td.div.table.tr.td" if name == "strong" => key = tag.text.toString().trim
          case "html.body.td.div.table.tr.td" if name == "a" => setValue(key, tag)
          case "html.body.td.div.table.tr" if tag.isClass("qb_line") && tag.text.nonEmpty => setValue(key, tag)
          case _ =>
        }
      }

      override def ontext(text: String) = tagStack.lastOption foreach (_.text.append(text))

      override def onend() = {
        //console.log("mappings => %s", JSON.dynamic.stringify(mappings, null, 4).asInstanceOf[String])
        val quote = for {
          exchange <- mappings.get("Exchange:")
          description <- mappings.get("Description:")
          contactInfo <- mappings.get("Contact Info:").map(_.split("[\n]").map(_.trim).toJSArray)
          ceoPresident <- mappings.get("CEO / President:")
          industrySector <- mappings.get("Industry/Sector:").map(_.split("[,]").map(_.trim).filter(_.nonEmpty).toJSArray)
        } yield {
          new YFProfile(
            symbol = symbol,
            responseTime = js.Date.now() - startTime)
        }
        promise.success(quote)
      }

      override def onerror(err: Error) = promise.failure(wrapJavaScriptException(err))

      private def tagPath = tagStack.map(_.name).mkString(".")

      private def setValue(key: String, tag: Tag) = {
        //console.log(s"$key [value] $tagPath:${tag.name} - ${tag.text.toString()}")
        mappings.get(key) match {
          case Some(value) => mappings(key) = value + ", " + tag.text.toString().trim
          case None => mappings(key) = tag.text.toString().trim
        }
      }

    }, new ParserOptions(decodeEntities = true, lowerCaseTags = true))

    parser.write(html)
    parser.end()
    promise.future
  }

}

/**
  * Yahoo! Finance Profile Service Companion
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
object YahooFinanceProfileService {

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
    * Represents a Yahoo! Finance profile
    * @author Lawrence Daniels <lawrence.daniels@gmail.com>
    */
  @ScalaJSDefined
  class YFProfile(val symbol: String,
                  val responseTime: Double) extends js.Object

}