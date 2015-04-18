package com.shocktrade.controllers

import play.api.Play._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json.Json.{obj => JS}
import play.api.libs.json._
import play.api.libs.ws.WS
import play.api.mvc._
import play.modules.reactivemongo.MongoController
import play.modules.reactivemongo.json.collection.JSONCollection

import scala.concurrent.Future

/**
 * Chart Resources
 * @author lawrence.daniels@gmail.com
 */
object ChartResources extends Controller with MongoController with MongoExtras {
  lazy val mcA: JSONCollection = db.collection[JSONCollection]("Avatars")
  lazy val mcC: JSONCollection = db.collection[JSONCollection]("Contests")
  lazy val mcQ: JSONCollection = db.collection[JSONCollection]("Stocks")

  def getAnalystRatings(symbol: String) = Action.async {
    getImageBinary(s"http://www.barchart.com/stocks/ratingsimg.php?sym=$symbol")
  }

  def getStockChart(symbol: String, size: String, range: String) = Action.async { request =>
    // construct the chart image URL
    val chartURL = size match {
      case "medium" =>
        s"http://chart.finance.yahoo.com/z?s=$symbol&t=$range&q=&l=&z=l&a=v&p=s&lang=en-US&region=US"
      case "small" =>
        s"http://chart.finance.yahoo.com/c/$range/d/$symbol"
      case _ =>
        throw new IllegalArgumentException(s"Invalid size argument '$size'")
    }

    // return the image to the client
    getImageBinary(chartURL)
  }

  private def getImageBinary(chartURL: String): Future[Result] = {
    WS.url(chartURL).getStream().map { case (response, body) =>
      // check that the response was successful
      if (response.status == 200) {
        // get the content type
        val contentType =
          response.headers.get("Content-Type").flatMap(_.headOption).getOrElse("application/octet-stream")

        // if there's a content length, send that, otherwise return the body chunked
        response.headers.get("Content-Length") match {
          case Some(Seq(length)) =>
            Ok.feed(body).as(contentType).withHeaders("Content-Length" -> length)
          case _ =>
            Ok.chunked(body).as(contentType)
        }
      } else {
        BadGateway
      }
    }
  }

  def getExposureByExchange(id: String, userName: String) = getExposureByXXX(id, userName, _.exchange)

  def getExposureByIndustry(id: String, userName: String) = getExposureByXXX(id, userName, _.industry)

  def getExposureBySector(id: String, userName: String) = getExposureByXXX(id, userName, _.sector)

  def getExposureBySecurities(id: String, userName: String) = getExposureByXXX(id, userName, _.symbol)

  private def getExposureByXXX(id: String, userName: String, fx: Position => String) = Action.async {
    for {
    // lookup the contest by ID
      contest <- mcC.findOneOpt(id) map (_.getOrElse(die("Game not found")))

      // lookup the participant
      participant = contest \ "participants" match {
        case JsArray(value) =>
          value find (p => (p \ "name").asOpt[String] == Some(userName)) getOrElse die(s"Player '$userName' not found")
        case _ => die(s"Player '$userName' not found")
      }

      // get the available funds
      fundsAvailable = (participant \ "fundsAvailable").asOpt[Double].map(v => trunc(v, 2)) getOrElse 0d

      // lookup the participant's positions
      positions = participant \ "positions" match {
        case JsArray(somePositions) => somePositions
        case _ => Seq.empty
      }

      // get the symbol & quantities for each position
      quantities = positions flatMap (pos =>
        for {symbol <- (pos \ "symbol").asOpt[String]; qty <- (pos \ "quantity").asOpt[Double]} yield (symbol, qty))

      // query the symbols for the current market price
      quotes <- QuoteResources.findQuotesBySymbols(quantities map (_._1))

      // create the mapping of symbols to quotes
      mappingQ = Map(quotes map (q => (q.symbol.getOrElse(""), q)): _*)

      // generate the value of each position
      posdata = quantities flatMap {
        case (symbol, qty) =>
          for {
            q <- mappingQ.get(symbol)
            exchange <- q.exchange
            lastTrade <- q.lastTrade
            sector <- q.sector
            industry <- q.industry
          } yield Position(symbol, exchange, sector, industry, lastTrade * qty)
      }

      // group the data
      groupeddata = posdata.groupBy(fx).foldLeft[List[(String, Double)]](List("Cash" -> fundsAvailable)) {
        case (list, (label, somePositions)) => (label, somePositions.map(_.value).sum) :: list
      }

      // produce the chart data
      values = groupeddata map { case (k, v) => JS("title" -> k, "value" -> v) }

    } yield Ok(JsArray(values))
  }

  /**
   * Truncates the given double to the given precision
   */
  private def trunc(value: Double, precision: Int): Double = {
    val s = math pow(10, precision)
    (math floor value * s) / s
  }

  case class Position(symbol: String, exchange: String, sector: String, industry: String, value: Double)

}