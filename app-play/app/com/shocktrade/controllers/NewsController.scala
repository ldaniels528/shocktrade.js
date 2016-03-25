package com.shocktrade.controllers

import java.text.SimpleDateFormat
import java.util.Date
import javax.inject.Inject

import com.shocktrade.dao.{RssFeedsDAO, SecuritiesDAO}
import play.api.Play._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json.Json.{obj => JS}
import play.api.libs.json._
import play.api.libs.ws.WS
import play.api.mvc._
import play.modules.reactivemongo.json.BSONFormats._
import play.modules.reactivemongo.{MongoController, ReactiveMongoApi, ReactiveMongoComponents}

import scala.concurrent.Future

/**
  * News REST Controller
  * @author lawrence.daniels@gmail.com
  */
class NewsController @Inject()(val reactiveMongoApi: ReactiveMongoApi) extends MongoController with ReactiveMongoComponents with ParsingCapabilities {
  private val securitiesDAO = SecuritiesDAO(reactiveMongoApi)
  private val rssFeedsDAO = RssFeedsDAO(reactiveMongoApi)

  ////////////////////////////////////////////////////////////////////////////
  //      API Functions
  ////////////////////////////////////////////////////////////////////////////

  /**
    * Returns the RSS feed for the given URL
    * @param id the given feed ID
    */
  def getFeed(id: String) = Action.async { request =>
    for {
    // load the source
      source_? <- rssFeedsDAO.findFeed(id)

      // extract the URL
      url = source_? flatMap (_.getAs[String]("url")) getOrElse "http://rss.cnn.com/rss/money_markets.rss"

      // get the content from the news service
      ws <- WS.url(url).get()

      // parse the RSS XML into JSON objects
      js <- parseRSS(ws.xml)

    // transform the RSS content into JSON
    } yield Ok(js)
  }

  /**
    * Retrieves the sources
    */
  def getSources = Action.async {
    rssFeedsDAO.findSources map (docs => Ok(Json.toJson(docs)))
  }

  /**
    * Parses an RSS channels
    */
  private def parseRSS(rss: scala.xml.Elem): Future[JsArray] = {
    Future.sequence((rss \ "channel") map { channel =>
      for {
      // parse the items
        items <- parseRSSItems(channel)

        // build the JSON channel
        js = JS("title" -> extract(channel, "title"),
          "description" -> extractText(channel, "description"),
          "link" -> extract(channel, "link"),
          "pubDate" -> (extract(channel, "pubDate") map parseDate),
          "lastBuildDate" -> (extract(channel, "lastBuildDate") map parseDate),
          "language" -> extract(channel, "language"),
          "ttl" -> (extract(channel, "ttl") map (_.toInt)),
          "items" -> items)
      } yield js
    }) map JsArray
  }

  private def parseRSSItems(channel: scala.xml.Node): Future[JsArray] = {
    Future.sequence((channel \ "item") map { item =>
      // get the item description
      val description = extractText(item, "description")

      // extract the tickers from the description
      val tickers = extractTickers(description)
      val symbols = (tickers map (_.symbol)).distinct

      securitiesDAO.findProductQuotes(symbols) map { quotes =>
        JS("title" -> extract(item, "title"),
          "description" -> description,
          "quotes" -> Json.toJson(quotes),
          "link" -> extract(item, "link"),
          "category" -> extract(item, "category"),
          "pubDate" -> (extract(item, "pubDate") map parseDate)) ++ parseRSSThumbNail(item)
      }
    }) map JsArray
  }

  /**
    * Parses a RSS Thumb nail image
    */
  private def parseRSSThumbNail(item: scala.xml.NodeSeq): JsObject = {
    ((item \ "thumbnail") map { xml =>
      JS("thumbNail" ->
        JS("url" -> extract(xml, s"@url"),
          "width" -> (extract(xml, s"@width") map (_.toInt)),
          "height" -> (extract(xml, s"@height") map (_.toInt))))
    }).headOption.getOrElse(JS())
  }

  private def toHtml(q: NewsQuote): String = {
    s"""<a href="/discover/${q.symbol}">
		<nobr><span class="${q.exchange}">${q.symbol} ${changeArrow(q.change)}</span>
		<span class="${changeClass(q.change)}">${q.changePct}%</span></nobr>
		</a>"""
  }

  /**
    * Extracts the text from the given XML sequence
    */
  private def extract(xml: scala.xml.NodeSeq, name: String): Option[String] = {
    (xml \ name).headOption map (_.text) map degunk
  }

  /**
    * Extracts the text from the given XML sequence
    */
  private def extractText(xml: scala.xml.NodeSeq, name: String): String = {
    degunk((xml \ name) map (_.text) mkString " ")
  }

  private def extractTickers(text: String): Seq[Ticker] = {
    var tickers: List[Ticker] = Nil
    var result: Option[(String, Int, Int)] = None
    do {
      // find the next image tag
      result = text.contents("(", ")", result map (_._3) getOrElse 0)
      result match {
        case Some((span, start, end)) =>
          val symbol = text.substring(start + 1, end - 1).trim
          if (allCaps(symbol) && symbol.length <= 7) {
            tickers = Ticker(symbol, start + 1, end - 1) :: tickers
          }
        case _ =>
      }
    } while (result.isDefined)
    tickers
  }

  /**
    * Removes HTML junk from a description
    */
  private def degunk(html: String): String = {
    val sb = new StringBuilder(html)
    var result: Option[(String, Int, Int)] = None
    do {
      // find the next image tag
      result = sb.contents("<", ">", result map (_._3) getOrElse 0)
      result match {
        case Some((span, start, end)) => sb.replace(start, end, "")
        case _ =>
      }
    } while (result.isDefined)

    sb.toString()
  }

  private def changeArrow(change: Double): String = {
    change match {
      case 0 => "&#8212;"
      case n if n > 0 => """<i class="fa fa-arrow-up positive"></i>"""
      case n if n < 0 => """<i class="fa fa-arrow-down negative"></i>"""
    }
  }

  private def changeClass(change: Double): String = {
    change match {
      case 0 => ""
      case n if n > 0 => "positive"
      case n if n < 0 => "negative"
    }
  }

  private def allCaps(s: String): Boolean = s.forall(_.isUpper)

  private def parseDate(s: String): Date = {
    import scala.util.Try

    // create the date parser
    val sdf1 = new SimpleDateFormat("EEE',' dd MMM yyyy HH:mm z") // Thu, 13 Mar 2014 03:23 GMT
    val sdf2 = new SimpleDateFormat("EEE',' dd MMM yyyy HH:mm:ss z") // Thu, 13 Mar 2014 03:23:11 EDT
    val sdf3 = new SimpleDateFormat("MM/dd/yy '-' hh:mm a z") // 05/04/14 - 1:03 AM EDT

    // heuristically determine the date format
    val d = s.replaceAllLiterally("EDT", "GMT-08:00")
    Try(sdf1.parse(d))
      .getOrElse(Try(sdf2.parse(d))
        .getOrElse(Try(sdf3.parse(d))
          .getOrElse(throw new IllegalArgumentException(s"Could not parse date '$s'"))))
  }

  private def toNewsQuote(js: JsObject): NewsQuote = {
    NewsQuote(
      (js \ "symbol").as[String],
      (js \ "exchange").as[String],
      (js \ "change").as[Double],
      (js \ "changePct").as[Double])
  }

  case class Ticker(symbol: String, start: Int, end: Int)

  case class NewsQuote(symbol: String, exchange: String, change: Double, changePct: Double)

}