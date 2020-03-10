package com.shocktrade.webapp.routes.contest

import com.shocktrade.common.models.ExposureData
import com.shocktrade.webapp.routes.NextFunction
import com.shocktrade.webapp.routes.contest.ChartRoutes._
import com.shocktrade.webapp.routes.discover.StockQuoteDAO
import io.scalajs.npm.express.{Application, Request, Response}

import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps
import scala.scalajs.js
import scala.util.{Failure, Success}

/**
 * Chart Routes
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
class ChartRoutes(app: Application)(implicit ec: ExecutionContext) {
  private val portfolioDAO = PortfolioDAO()
  private val securitiesDAO = StockQuoteDAO()

  app.get("/api/charts/exposure/exchange/:id/:userID", (request: Request, response: Response, next: NextFunction) => exposureByExchange(request, response, next))
  app.get("/api/charts/exposure/industry/:id/:userID", (request: Request, response: Response, next: NextFunction) => exposureByIndustry(request, response, next))
  app.get("/api/charts/exposure/market/:id/:userID", (request: Request, response: Response, next: NextFunction) => exposureByMarket(request, response, next))
  app.get("/api/charts/exposure/sector/:id/:userID", (request: Request, response: Response, next: NextFunction) => exposureBySector(request, response, next))
  app.get("/api/charts/exposure/securities/:id/:userID", (request: Request, response: Response, next: NextFunction) => exposureBySecurities(request, response, next))

  //////////////////////////////////////////////////////////////////////////////////////
  //      API Methods
  //////////////////////////////////////////////////////////////////////////////////////

  def exposureByExchange(request: Request, response: Response, next: NextFunction): Unit = {
    val contestId = request.params("id")
    val portfolioID = request.params("userID")
    getExposureByXXX(contestId, portfolioID, _.exchange) onComplete {
      case Success(data) => response.send(data); next()
      case Failure(e) => response.internalServerError(e); next()
    }
  }

  def exposureByIndustry(request: Request, response: Response, next: NextFunction): Unit = {
    val contestId = request.params("id")
    val portfolioID = request.params("userID")
    getExposureByXXX(contestId, portfolioID, _.industry) onComplete {
      case Success(data) => response.send(data); next()
      case Failure(e) => response.internalServerError(e); next()
    }
  }

  def exposureByMarket(request: Request, response: Response, next: NextFunction): Unit = {
    val contestId = request.params("id")
    val portfolioID = request.params("userID")
    getExposureByXXX(contestId, portfolioID, _.market) onComplete {
      case Success(data) => response.send(data); next()
      case Failure(e) => response.internalServerError(e); next()
    }
  }

  def getExposureByXXX(contestId: String, portfolioID: String, fx: RawData => String): Future[js.Array[ExposureData]] = {
    /*
    for {
      // lookup the portfolio by the contest and player IDs
      portfolio <- portfolioDAO.findOneByContest(contestId, portfolioID) map (_ orDie "Portfolio not found")

      // get the symbol & quantities for each position
      quantities = portfolio.positions.toList.flatMap(_ map (pos => (pos.symbol.orNull, pos.quantity.getOrElse(0d))))

      // query the symbols for the current market price
      quotes <- securitiesDAO.findQuotes[ExposureQuote](quantities map (_._1))

      // create the mapping of symbols to quotes
      mappingQ = Map(quotes map (q => q.symbol -> q): _*)

      // generate the raw data for each position
      rawData = quantities flatMap { case (symbol, qty) =>
        (for {
          q <- mappingQ.get(symbol).orUndefined
          exchange <- q.exchange
          market <- q.exchange
          lastTrade <- q.lastTrade
          sector <- q.sector
          industry <- q.industry
        } yield RawData(symbol, exchange, market, sector, industry, lastTrade * qty)) toList
      }

      // group the data into tuples
      tupleData = ("Cash" -> portfolio.cashAccount.flatMap(_.funds).getOrElse(0d)) ::
        rawData.groupBy(fx).foldLeft[List[(String, Double)]](Nil) {
          case (list, (label, somePositions)) => (label, somePositions.map(_.value).sum) :: list
        }

      total = tupleData map (_._2) sum

      // produce the chart data
      values = tupleData map { case (k, v) =>
        val pct = 100 * (v / total)
        new ExposureData(key = f"$k ($pct%.1f%%) ", value = pct)
      }

    } yield js.Array(values: _*)*/
    ???
  }

  def exposureBySector(request: Request, response: Response, next: NextFunction): Unit = {
    val contestId = request.params("id")
    val portfolioID = request.params("userID")
    getExposureByXXX(contestId, portfolioID, _.sector) onComplete {
      case Success(data) => response.send(data); next()
      case Failure(e) => response.internalServerError(e); next()
    }
  }

  //////////////////////////////////////////////////////////////////////////////////////
  //      Private Methods
  //////////////////////////////////////////////////////////////////////////////////////

  def exposureBySecurities(request: Request, response: Response, next: NextFunction): Unit = {
    val contestId = request.params("id")
    val portfolioID = request.params("userID")
    getExposureByXXX(contestId, portfolioID, _.symbol) onComplete {
      case Success(data) => response.send(data); next()
      case Failure(e) => response.internalServerError(e); next()
    }
  }

}

/**
 * Chart Routes Companion
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
object ChartRoutes {

  case class RawData(symbol: String, exchange: String, market: String, sector: String, industry: String, value: Double)

}
