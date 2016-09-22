package com.shocktrade.webapp.routes

import com.shocktrade.common.dao.contest.PortfolioDAO._
import com.shocktrade.common.dao.securities.SecuritiesDAO._
import com.shocktrade.common.models.ExposureData
import com.shocktrade.common.models.quote.ExposureQuote
import org.scalajs.nodejs.NodeRequire
import org.scalajs.nodejs.express.{Application, Request, Response}
import org.scalajs.nodejs.mongodb.{Db, MongoDB}
import org.scalajs.sjs.OptionHelper._

import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps
import scala.scalajs.js
import scala.scalajs.js.JSConverters._
import scala.util.{Failure, Success}

/**
  * Chart Routes
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
object ChartRoutes {

  def init(app: Application, dbFuture: Future[Db])(implicit ec: ExecutionContext, mongo: MongoDB, require: NodeRequire) = {
    implicit val portfolioDAO = dbFuture.flatMap(_.getPortfolioDAO)
    implicit val securitiesDAO = dbFuture.flatMap(_.getSecuritiesDAO)

    app.get("/api/charts/exposure/exchange/:id/:userID", (request: Request, response: Response, next: NextFunction) => exposureByExchange(request, response, next))
    app.get("/api/charts/exposure/industry/:id/:userID", (request: Request, response: Response, next: NextFunction) => exposureByIndustry(request, response, next))
    app.get("/api/charts/exposure/market/:id/:userID", (request: Request, response: Response, next: NextFunction) => exposureByMarket(request, response, next))
    app.get("/api/charts/exposure/sector/:id/:userID", (request: Request, response: Response, next: NextFunction) => exposureBySector(request, response, next))
    app.get("/api/charts/exposure/securities/:id/:userID", (request: Request, response: Response, next: NextFunction) => exposureBySecurities(request, response, next))

    //////////////////////////////////////////////////////////////////////////////////////
    //      API Methods
    //////////////////////////////////////////////////////////////////////////////////////

    def exposureByExchange(request: Request, response: Response, next: NextFunction) = {
      val contestId = request.params("id")
      val playerId = request.params("userID")
      getExposureByXXX(contestId, playerId, _.exchange) onComplete {
        case Success(data) => response.send(data); next()
        case Failure(e) => response.internalServerError(e); next()
      }
    }

    def exposureByIndustry(request: Request, response: Response, next: NextFunction) = {
      val contestId = request.params("id")
      val playerId = request.params("userID")
      getExposureByXXX(contestId, playerId, _.industry) onComplete {
        case Success(data) => response.send(data); next()
        case Failure(e) => response.internalServerError(e); next()
      }
    }

    def exposureByMarket(request: Request, response: Response, next: NextFunction) = {
      val contestId = request.params("id")
      val playerId = request.params("userID")
      getExposureByXXX(contestId, playerId, _.market) onComplete {
        case Success(data) => response.send(data); next()
        case Failure(e) => response.internalServerError(e); next()
      }
    }

    def exposureBySector(request: Request, response: Response, next: NextFunction) = {
      val contestId = request.params("id")
      val playerId = request.params("userID")
      getExposureByXXX(contestId, playerId, _.sector) onComplete {
        case Success(data) => response.send(data); next()
        case Failure(e) => response.internalServerError(e); next()
      }
    }

    def exposureBySecurities(request: Request, response: Response, next: NextFunction) = {
      val contestId = request.params("id")
      val playerId = request.params("userID")
      getExposureByXXX(contestId, playerId, _.symbol) onComplete {
        case Success(data) => response.send(data); next()
        case Failure(e) => response.internalServerError(e); next()
      }
    }

    //////////////////////////////////////////////////////////////////////////////////////
    //      Private Methods
    //////////////////////////////////////////////////////////////////////////////////////

    def getExposureByXXX(contestId: String, playerId: String, fx: RawData => String) = {
      for {
      // lookup the portfolio by the contest and player IDs
        portfolio <- portfolioDAO.flatMap(_.findOneByPlayer(contestId, playerId)) map (_ orDie "Portfolio not found")

        // get the symbol & quantities for each position
        quantities = portfolio.positions.toList.flatMap(_ map (pos => (pos.symbol.orNull, pos.quantity.getOrElse(0d))))

        // query the symbols for the current market price
        quotes <- securitiesDAO.flatMap(_.findQuotesBySymbols[ExposureQuote](quantities map (_._1), fields = ExposureQuote.Fields))

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
        tupleData = ("Cash" -> portfolio.cashAccount.flatMap(_.cashFunds).getOrElse(0d)) ::
          rawData.groupBy(fx).foldLeft[List[(String, Double)]](Nil) {
            case (list, (label, somePositions)) => (label, somePositions.map(_.value).sum) :: list
          }

        total = tupleData map (_._2) sum

        // produce the chart data
        values = tupleData map { case (k, v) =>
          val pct = 100 * (v / total)
          new ExposureData(key = f"$k ($pct%.1f%%) ", value = pct)
        }

      } yield js.Array(values: _*)
    }

  }

  case class RawData(symbol: String, exchange: String, market: String, sector: String, industry: String, value: Double)

}
