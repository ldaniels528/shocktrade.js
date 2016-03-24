package com.shocktrade.controllers

import javax.inject.Inject

import akka.util.Timeout
import com.github.ldaniels528.commons.helpers.OptionHelper._
import com.shocktrade.dao.SecuritiesDAO
import com.shocktrade.server.trading.ContestDAO
import com.shocktrade.util.BSONHelper._
import com.shocktrade.util.ConcurrentCache
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json.Json.{obj => JS}
import play.api.libs.json._
import play.api.libs.ws.WSClient
import play.api.mvc._
import play.modules.reactivemongo.{MongoController, ReactiveMongoApi, ReactiveMongoComponents}
import reactivemongo.bson.BSONObjectID

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.language.postfixOps

/**
  * Chart Resources
  * @author lawrence.daniels@gmail.com
  */
class ChartController @Inject()(val reactiveMongoApi: ReactiveMongoApi, val ws: WSClient)
  extends MongoController with ReactiveMongoComponents {

  private val contestDAO = ContestDAO(reactiveMongoApi)
  private val securitiesDAO = SecuritiesDAO(reactiveMongoApi)
  private val analystCharts = ConcurrentCache[String, Future[Result]](3.days)
  private val stockCharts = ConcurrentCache[String, Future[Result]](15.minutes)

  def getAnalystRatings(symbol: String) = Action.async {
    analystCharts.getOrElseUpdate(symbol, getImageBinary(s"http://www.barchart.com/stocks/ratingsimg.php?sym=$symbol")).get
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
    stockCharts.getOrElseUpdate(chartURL, getImageBinary(chartURL)).get
  }

  private def getImageBinary(chartURL: String): Future[Result] = {
    ws.url(chartURL).getStream().map { case (response, body) =>
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

  def getExposureByExchange(contestId: String, userName: String) = getExposureByXXX(contestId.toBSID, userName.toBSID, _.exchange)

  def getExposureByIndustry(contestId: String, userName: String) = getExposureByXXX(contestId.toBSID, userName.toBSID, _.industry)

  def getExposureByMarket(contestId: String, userName: String) = getExposureByXXX(contestId.toBSID, userName.toBSID, _.market)

  def getExposureBySector(contestId: String, userName: String) = getExposureByXXX(contestId.toBSID, userName.toBSID, _.sector)

  def getExposureBySecurities(contestId: String, userName: String) = getExposureByXXX(contestId.toBSID, userName.toBSID, _.symbol)

  private def getExposureByXXX(contestId: BSONObjectID, userId: BSONObjectID, fx: Position => String) = Action.async {
    implicit val timeout: Timeout = 10.seconds
    for {
    // lookup the contest by ID
      contest <- contestDAO.findContestByID(contestId) map (_ orDie "Game not found")

      // lookup the participant
      participant = contest.participants.find(_.id == userId) orDie s"Player '${userId.stringify}' not found"

      // get the symbol & quantities for each position
      quantities = participant.positions map (pos => (pos.symbol, pos.quantity))

      // query the symbols for the current market price
      quotes <- securitiesDAO.findQuotesBySymbols(quantities map (_._1))

      // create the mapping of symbols to quotes
      mappingQ = Map(quotes map (q => (q.symbol, q)): _*)

      // generate the value of each position
      posData = quantities flatMap {
        case (symbol, qty) =>
          for {
            q <- mappingQ.get(symbol)
            exchange <- q.exchange
            market <- q.market
            lastTrade <- q.lastTrade
            sector <- q.sector
            industry <- q.industry
          } yield Position(symbol, exchange, market, sector, industry, lastTrade * qty)
      }

      // group the data
      groupedData = ("Cash" -> participant.cashAccount.cashFunds.toDouble) :: posData.groupBy(fx).foldLeft[List[(String, Double)]](Nil) {
        case (list, (label, somePositions)) => (label, somePositions.map(_.value).sum) :: list
      }

      total = groupedData map (_._2) sum

      // produce the chart data
      values = groupedData map { case (k, v) =>
        val pct = 100d * (v / total)
        JS("label" -> f"$k ($pct%.1f%%)", "value" -> pct)
      }

    } yield Ok(JsArray(values))
  }

  case class Position(symbol: String, exchange: String, market: String, sector: String, industry: String, value: Double)

}