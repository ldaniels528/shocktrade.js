package com.shocktrade.webapp.routes

import com.shocktrade.common.dao._
import com.shocktrade.common.dao.contest.ContestDAO._
import com.shocktrade.common.dao.contest.PerksDAO._
import com.shocktrade.common.dao.contest.PortfolioDAO._
import com.shocktrade.common.dao.contest.{ContestData, OrderData, PortfolioData}
import com.shocktrade.common.forms.{NewOrderForm, PerksResponse}
import com.shocktrade.common.models.contest.{PortfolioRanking, PositionLike, TotalInvestment}
import com.shocktrade.server.services.yahoo.YahooFinanceCSVQuotesService
import com.shocktrade.server.services.yahoo.YahooFinanceCSVQuotesService.YFCSVQuote
import com.shocktrade.util.StringHelper._
import org.scalajs.nodejs.express.{Application, Request, Response}
import org.scalajs.nodejs.mongodb.{Db, MongoDB}
import org.scalajs.nodejs.util.ScalaJsHelper._
import org.scalajs.nodejs.{NodeRequire, console}

import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps
import scala.scalajs.js
import scala.scalajs.js.JSConverters._
import scala.util.{Failure, Success}

/**
  * Portfolio Routes
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
object PortfolioRoutes {

  /**
    * Initializes the routes
    */
  def init(app: Application, dbFuture: Future[Db])(implicit ec: ExecutionContext, require: NodeRequire, mongo: MongoDB) = {
    val contestDAO = dbFuture.flatMap(_.getContestDAO)
    val perkDAO = dbFuture.flatMap(_.getPerksDAO)
    val portfolioDAO = dbFuture.flatMap(_.getPortfolioDAO)
    val yfCsvQuoteSvc = new YahooFinanceCSVQuotesService()
    val cvsParams = yfCsvQuoteSvc.getParams("symbol", "exchange", "lastTrade", "open", "close", "tradeDate", "tradeTime", "volume")

    // individual objects
    app.get("/api/portfolio/contest/:contestID/player/:playerID", (request: Request, response: Response, next: NextFunction) => portfolioByPlayer(request, response, next))
    app.delete("/api/portfolio/:portfolioID/order/:orderID", (request: Request, response: Response, next: NextFunction) => cancelOrder(request, response, next))
    app.post("/api/portfolio/:portfolioID/order", (request: Request, response: Response, next: NextFunction) => createOrder(request, response, next))
    app.get("/api/portfolio/:portfolioID/perks", (request: Request, response: Response, next: NextFunction) => perksByID(request, response, next))
    app.post("/api/portfolio/:portfolioID/perks", (request: Request, response: Response, next: NextFunction) => purchasePerks(request, response, next))
    app.get("/api/portfolio/:portfolioID/positions", (request: Request, response: Response, next: NextFunction) => positionsByID(request, response, next))
    app.get("/api/portfolio/:playerID/positions/symbols", (request: Request, response: Response, next: NextFunction) => heldSecurities(request, response, next))

    // collections
    app.get("/api/portfolios/contest/:contestID", (request: Request, response: Response, next: NextFunction) => portfoliosByContest(request, response, next))
    app.get("/api/portfolios/contest/:contestID/rankings", (request: Request, response: Response, next: NextFunction) => rankingsByContest(request, response, next))
    app.get("/api/portfolios/player/:playerID", (request: Request, response: Response, next: NextFunction) => portfoliosByPlayer(request, response, next))
    app.get("/api/portfolios/player/:playerID/totalInvestment", (request: Request, response: Response, next: NextFunction) => totalInvestment(request, response, next))

    //////////////////////////////////////////////////////////////////////////////////////
    //      API Methods
    //////////////////////////////////////////////////////////////////////////////////////

    def cancelOrder(request: Request, response: Response, next: NextFunction) = {
      val portfolioID = request.params("portfolioID")
      val orderID = request.params("orderID")
      portfolioDAO.flatMap(_.cancelOrder(portfolioID, orderID)) onComplete {
        case Success(results) if results.isOk && results.value != null => response.send(results.value); next()
        case Success(results) =>
          console.error("failed result = %j", results)
          response.notFound(s"Order for portfolio # $portfolioID")
          next()
        case Failure(e) => response.internalServerError(e); next()
      }
    }

    def createOrder(request: Request, response: Response, next: NextFunction) = {
      val portfolioID = request.params("portfolioID")
      val form = request.bodyAs[NewOrderForm]

      form.validate match {
        case messages if messages.nonEmpty => response.badRequest(messages); next()
        case _ =>
          val order = form.toOrder
          portfolioDAO.flatMap(_.createOrder(portfolioID, order)) onComplete {
            case Success(results) if results.isOk && results.value != null => response.send(results.value); next()
            case Success(results) =>
              console.error("failed result = %j", results)
              response.notFound(s"Order for portfolio # $portfolioID")
              next()
            case Failure(e) => response.internalServerError(e); next()
          }
      }
    }

    def heldSecurities(request: Request, response: Response, next: NextFunction) = {
      val playerID = request.params("playerID")
      portfolioDAO.flatMap(_.findHeldSecurities(playerID)) onComplete {
        case Success(symbols) => response.send(symbols); next()
        case Failure(e) => response.internalServerError(e); next()
      }
    }

    def perksByID(request: Request, response: Response, next: NextFunction) = {
      val portfolioID = request.params("portfolioID")
      val outcome = for {
        portfolio <- portfolioDAO.flatMap(_.findPerks(portfolioID))
        perksResponse = portfolio.map { p =>
          new PerksResponse(
            fundsAvailable = p.cashAccount.flatMap(_.cashFunds),
            perkCodes = p.perks getOrElse emptyArray)
        }
      } yield perksResponse

      outcome onComplete {
        case Success(Some(result)) => response.send(result); next()
        case Success(None) => response.notFound(s"Portfolio: $portfolioID"); next()
        case Failure(e) => response.internalServerError(e); next()
      }
    }

    def purchasePerks(request: Request, response: Response, next: NextFunction) = {
      val portfolioID = request.params("portfolioID")
      val purchasePerkCodes = request.bodyAs[js.Array[String]]
      val outcome = for {
        perks <- perkDAO.flatMap(_.findAvailablePerks)
        perkMapping = js.Dictionary(perks.map(p => p.code.orNull -> p): _*)
        perksCost = (purchasePerkCodes flatMap perkMapping.get).flatMap(_.cost.toOption).sum
        response <- portfolioDAO.flatMap(_.purchasePerks(portfolioID, purchasePerkCodes, perksCost).toFuture)
      } yield response

      outcome onComplete {
        case Success(result) if result.isOk =>
          result.valueAs[PortfolioData] match {
            case Some(portfolio) => response.send(portfolio)
            case None =>
              console.log("notFound: failed result = %j", result)
              response.notFound("Purchased failed")
              next()
          }
          next()
        case Success(result) =>
          console.log("badRequest: failed result = %j", result)
          response.badRequest("Purchased failed")
          next()
        case Failure(e) => response.internalServerError(e); next()
      }
    }

    def positionsByID(request: Request, response: Response, next: NextFunction) = {
      val portfolioID = request.params("portfolioID")
      portfolioDAO.flatMap(_.findPositions(portfolioID)) onComplete {
        case Success(Some(positions)) => response.send(positions); next()
        case Success(None) => response.notFound(s"Portfolio: $portfolioID"); next()
        case Failure(e) => response.internalServerError(e); next()
      }
    }

    /**
      * Retrieves a portfolio by a contest ID and player ID
      */
    def portfolioByPlayer(request: Request, response: Response, next: NextFunction) = {
      val contestID = request.params("contestID")
      val playerID = request.params("playerID")
      portfolioDAO.flatMap(_.findOneByPlayer(contestID, playerID)) onComplete {
        case Success(Some(portfolio)) => response.send(portfolio); next()
        case Success(None) => response.notFound(s"Contest: $contestID, Player: $playerID"); next()
        case Failure(e) => response.internalServerError(e); next()
      }
    }

    /**
      * Retrieves a collection of portfolios by a contest ID
      */
    def portfoliosByContest(request: Request, response: Response, next: NextFunction) = {
      val contestID = request.params("contestID")
      portfolioDAO.flatMap(_.findByContest(contestID)) onComplete {
        case Success(portfolios) => response.send(portfolios); next()
        case Failure(e) => response.internalServerError(e); next()
      }
    }

    /**
      * Retrieves a collection of portfolios by a player ID
      */
    def portfoliosByPlayer(request: Request, response: Response, next: NextFunction) = {
      val playerID = request.params("playerID")
      portfolioDAO.flatMap(_.findByPlayer(playerID)) onComplete {
        case Success(portfolios) => response.send(portfolios); next()
        case Failure(e) => response.internalServerError(e); next()
      }
    }

    /**
      * Retrieves a collection of rankings by contest
      */
    def rankingsByContest(request: Request, response: Response, next: NextFunction) = {
      val contestID = request.params("contestID")
      val outcome = for {
        contest <- contestDAO.flatMap(_.findById[ContestData](contestID))
        portfolios <- portfolioDAO.flatMap(_.findByContest(contestID)).map(_.toList)
        symbols = portfolios.flatMap(_.positions.toList.flatMap(_.toList)).flatMap(_.symbol.toOption).distinct
        quotes <- yfCsvQuoteSvc.getQuotes(cvsParams, symbols: _*)
        mapping = Map(quotes.map(q => q.symbol -> q): _*)

        rankings = portfolios map { portfolio =>
          val player = contest.flatMap(_.participants.toOption).flatMap(_.find(_._id == portfolio.playerID)).orUndefined
          val positions = portfolio.positions.toList.flatMap(_.toList)
          val totalInvestment = computeInvestment(positions, mapping)

          val startingBalance = contest.flatMap(_.startingBalance.toOption).orUndefined
          val cashFunds = portfolio.cashAccount.flatMap(_.cashFunds)
          val totalEquity = cashFunds.map(_ + totalInvestment)
          val gainLoss_% = for {bal <- startingBalance; equity <- totalEquity} yield 100 * ((equity - bal) / bal)

          new PortfolioRanking(
            _id = portfolio.playerID,
            facebookID = player.flatMap(_.facebookID),
            name = player.flatMap(_.name),
            rank = js.undefined,
            totalEquity = totalEquity,
            gainLoss = gainLoss_%)
        }

        // sort the rankings and add the position (e.g. "1st")
        sortedRankings = {
          val myRankings = rankings.sortBy(-_.gainLoss.getOrElse(0d))
          myRankings.zipWithIndex foreach { case (ranking, index) =>
            ranking.rank = (index + 1) nth
          }
          js.Array(myRankings: _*)
        }

      } yield sortedRankings

      outcome onComplete {
        case Success(rankings) => response.send(rankings); next()
        case Failure(e) => response.internalServerError(e); next()
      }
    }

    /**
      * Retrieves the total investment amount for a specific player
      */
    def totalInvestment(request: Request, response: Response, next: NextFunction) = {
      val playerID = request.params("playerID")
      portfolioDAO.flatMap(_.totalInvestment(playerID)) onComplete {
        case Success(investment) => response.send(new TotalInvestment(investment)); next()
        case Failure(e) => response.internalServerError(e); next()
      }
    }

  }

  private def computeInvestment(positions: Seq[PositionLike], mapping: Map[String, YFCSVQuote]) = {
    positions flatMap { p =>
      (for {
        symbol <- p.symbol
        quantity <- p.quantity
        pricePaid <- p.pricePaid

        quote <- mapping.get(symbol).orUndefined
        lastTrade <- quote.lastTrade
      } yield lastTrade * quantity) toOption
    } sum
  }

  /**
    * New Order Form Extensions
    * @param form the given [[NewOrderForm form]]
    */
  implicit class NewOrderFormExtensions(val form: NewOrderForm) extends AnyVal {

    def toOrder = {
      new OrderData(
        _id = js.undefined,
        symbol = form.symbol,
        exchange = form.exchange,
        accountType = form.accountType,
        orderType = form.orderType,
        priceType = form.priceType,
        price = if (form.isLimitOrder) form.limitPrice else js.undefined,
        quantity = form.quantity,
        creationTime = new js.Date(),
        expirationTime = js.undefined, // TODO need to interpret order term
        processedTime = js.undefined,
        statusMessage = js.undefined)
    }

  }

}
