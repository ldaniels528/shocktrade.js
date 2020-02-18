package com.shocktrade.webapp.routes

import com.shocktrade.common.forms.{FundsTransferRequest, NewOrderForm, PerksResponse}
import com.shocktrade.common.models.contest.{MarketValueResponse, Participant, PositionLike, TotalInvestment}
import com.shocktrade.common.util.StringHelper._
import com.shocktrade.server.dao.contest.PortfolioDAO._
import com.shocktrade.server.dao.contest._
import com.shocktrade.server.dao.securities.SecuritiesDAO
import com.shocktrade.server.facade.PricingQuote
import com.shocktrade.server.services.yahoo.YahooFinanceCSVQuotesService
import com.shocktrade.server.services.yahoo.YahooFinanceCSVQuotesService.YFCSVQuote
import io.scalajs.nodejs.console
import io.scalajs.npm.express.{Application, Request, Response}
import io.scalajs.npm.mongodb.Db
import io.scalajs.util.DateHelper._
import io.scalajs.util.OptionHelper._
import io.scalajs.util.PromiseHelper.Implicits._
import io.scalajs.util.ScalaJsHelper._

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps
import scala.scalajs.js
import scala.scalajs.js.JSConverters._
import scala.util.{Failure, Success, Try}

/**
  * Portfolio Routes
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
object PortfolioRoutes {

  /**
    * Initializes the routes
    */
  def init(app: Application, dbFuture: Future[Db])(implicit ec: ExecutionContext): Unit = {
    val contestDAO = ContestDAO()
    val perkDAO = PerksDAO()
    val portfolioDAO = dbFuture.map(_.getPortfolioDAO)
    val securitiesDAO = dbFuture.map(SecuritiesDAO.apply)
    val yfCsvQuoteSvc = new YahooFinanceCSVQuotesService()
    val cvsParams = yfCsvQuoteSvc.getParams("symbol", "exchange", "lastTrade", "open", "close", "tradeDate", "tradeTime", "volume")

    // individual objects
    app.get("/api/portfolio/contest/:contestID/player/:playerID", (request: Request, response: Response, next: NextFunction) => portfolioByPlayer(request, response, next))
    app.get("/api/portfolio/:portfolioID/marketValue", (request: Request, response: Response, next: NextFunction) => computeMarketValue(request, response, next))
    app.delete("/api/portfolio/:portfolioID/order/:orderID", (request: Request, response: Response, next: NextFunction) => cancelOrder(request, response, next))
    app.post("/api/portfolio/:portfolioID/order", (request: Request, response: Response, next: NextFunction) => createOrder(request, response, next))
    app.get("/api/portfolio/:portfolioID/perks", (request: Request, response: Response, next: NextFunction) => perksByID(request, response, next))
    app.post("/api/portfolio/:portfolioID/perks", (request: Request, response: Response, next: NextFunction) => purchasePerks(request, response, next))
    app.get("/api/portfolio/:portfolioID/positions", (request: Request, response: Response, next: NextFunction) => positionsByID(request, response, next))
    app.get("/api/portfolio/:playerID/heldSecurities", (request: Request, response: Response, next: NextFunction) => heldSecurities(request, response, next))
    app.post("/api/portfolio/:portfolioID/transferFunds", (request: Request, response: Response, next: NextFunction) => transferFunds(request, response, next))

    // collections
    app.get("/api/portfolios/contest/:contestID", (request: Request, response: Response, next: NextFunction) => portfoliosByContest(request, response, next))
    app.get("/api/portfolios/contest/:contestID/rankings", (request: Request, response: Response, next: NextFunction) => rankingsByContest(request, response, next))
    app.get("/api/portfolios/player/:playerID", (request: Request, response: Response, next: NextFunction) => portfoliosByPlayer(request, response, next))
    app.get("/api/portfolios/player/:playerID/totalInvestment", (request: Request, response: Response, next: NextFunction) => totalInvestment(request, response, next))

    //////////////////////////////////////////////////////////////////////////////////////
    //      API Methods
    //////////////////////////////////////////////////////////////////////////////////////

    /**
      * Cancels an order by portfolio and order IDs
      */
    def cancelOrder(request: Request, response: Response, next: NextFunction): Unit = {
      val portfolioID = request.params.apply("portfolioID")
      val orderID = request.params.apply("orderID")
      portfolioDAO.flatMap(_.cancelOrder(portfolioID, orderID)) onComplete {
        case Success(results) if results.isOk && results.value != null => response.send(results.value); next()
        case Success(results) =>
          console.error("failed result = %j", results)
          response.notFound(s"Order for portfolio # $portfolioID")
          next()
        case Failure(e) => response.internalServerError(e); next()
      }
    }

    /**
      * Computes the market value of an account by account type and portfolio ID
      */
    def computeMarketValue(request: Request, response: Response, next: NextFunction): Unit = {
      val portfolioID = request.params.apply("portfolioID")
      val accountFilter: PositionData => Boolean = request.query.get("accountType") match {
        case Some("cash") => _.isCashAccount
        case Some("margin") => _.isMarginAccount
        case Some("all") => _ => true
        case Some(accountType) => die(s"Invalid account type '$accountType'")
        case None => die("Missing account type")
      }

      val outcome = for {
        positionsOpt <- portfolioDAO.flatMap(_.findPositions(portfolioID))
        posData = for {
          position <- positionsOpt.orDie(s"Portfolio # $portfolioID not found").filter(accountFilter)
          symbol <- position.symbol.toList
          pricePaid <- position.pricePaid.toList
          quantity <- position.quantity.toList
        } yield PricingData(symbol, quantity, pricePaid)
        quotes <- securitiesDAO.flatMap(_.findQuotesBySymbols[PricingQuote](posData.map(_.symbol), fields = PricingQuote.Fields))
      } yield {
        val mapping = Map(posData.map(p => p.symbol -> p): _*)
        quotes flatMap (q => mapping.get(q.symbol) map (p => p.quantity * q.lastTrade.getOrElse(p.pricePaid))) sum
      }

      outcome onComplete {
        case Success(marketValue) => response.send(new MarketValueResponse(marketValue)); next()
        case Failure(e: IllegalStateException) => response.badRequest(e.getMessage); next()
        case Failure(e) => response.internalServerError(e); next()
      }
    }

    /**
      * Creates a new order within a portfolio by portfolio ID
      */
    def createOrder(request: Request, response: Response, next: NextFunction): Unit = {
      val portfolioID = request.params.apply("portfolioID")
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

    /**
      * Returns the symbols for securities currently held in all active portfolios for a given player by player ID
      */
    def heldSecurities(request: Request, response: Response, next: NextFunction): Unit = {
      val playerID = request.params.apply("playerID")
      portfolioDAO.flatMap(_.findHeldSecurities(playerID)) onComplete {
        case Success(symbols) => response.send(symbols); next()
        case Failure(e) => response.internalServerError(e); next()
      }
    }

    /**
      * Retrieves the purchased perks by portfolio ID
      */
    def perksByID(request: Request, response: Response, next: NextFunction): Unit = {
      val portfolioID = request.params.apply("portfolioID")
      val outcome = for {
        portfolio <- portfolioDAO.flatMap(_.findPerks(portfolioID))
        perksResponse = portfolio.map { p =>
          new PerksResponse(
            fundsAvailable = p.cashAccount.flatMap(_.funds),
            perkCodes = p.perks getOrElse emptyArray)
        }
      } yield perksResponse

      outcome onComplete {
        case Success(Some(result)) => response.send(result); next()
        case Success(None) => response.notFound(s"Portfolio: $portfolioID"); next()
        case Failure(e) => response.internalServerError(e); next()
      }
    }

    /**
      * Purchases perks via an array of perk codes and a portfolio ID
      */
    def purchasePerks(request: Request, response: Response, next: NextFunction): Unit = {
      val portfolioID = request.params.apply("portfolioID")
      val purchasePerkCodes = request.bodyAs[js.Array[String]]
      val outcome = for {
        perks <- perkDAO.findAvailablePerks
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

    /**
      * Retrieves all positions (cash or margin accounts) by portfolio ID
      */
    def positionsByID(request: Request, response: Response, next: NextFunction): Unit = {
      val portfolioID = request.params.apply("portfolioID")
      portfolioDAO.flatMap(_.findPositions(portfolioID)) onComplete {
        case Success(Some(positions)) => response.send(positions); next()
        case Success(None) => response.notFound(s"Portfolio: $portfolioID"); next()
        case Failure(e) => response.internalServerError(e); next()
      }
    }

    /**
      * Retrieves a portfolio by a contest ID and player ID
      */
    def portfolioByPlayer(request: Request, response: Response, next: NextFunction): Unit = {
      val contestID = request.params.apply("contestID")
      val playerID = request.params.apply("playerID")
      portfolioDAO.flatMap(_.findOneByPlayer(contestID, playerID)) onComplete {
        case Success(Some(portfolio)) => response.send(portfolio); next()
        case Success(None) => response.notFound(s"Contest: $contestID, Player: $playerID"); next()
        case Failure(e) => response.internalServerError(e); next()
      }
    }

    /**
      * Retrieves a collection of portfolios by a contest ID
      */
    def portfoliosByContest(request: Request, response: Response, next: NextFunction): Unit = {
      val contestID = request.params.apply("contestID")
      portfolioDAO.flatMap(_.findByContest(contestID)) onComplete {
        case Success(portfolios) => response.send(portfolios); next()
        case Failure(e) => response.internalServerError(e); next()
      }
    }

    /**
      * Retrieves a collection of portfolios by a player ID
      */
    def portfoliosByPlayer(request: Request, response: Response, next: NextFunction): Unit = {
      val playerID = request.params.apply("playerID")
      portfolioDAO.flatMap(_.findByPlayer(playerID)) onComplete {
        case Success(portfolios) => response.send(portfolios); next()
        case Failure(e) => response.internalServerError(e); next()
      }
    }

    /**
      * Retrieves a collection of rankings by contest
      */
    def rankingsByContest(request: Request, response: Response, next: NextFunction): Unit = {
      val contestID = request.params.apply("contestID")
      val outcome = for {
        contest <- contestDAO.findOneByID(contestID)
        _ = console.log(s"contest = %j", contest)
        portfolios <- portfolioDAO.flatMap(_.findByContest(contestID)).map(_.toList)
        _ = console.log(s"portfolios = %j", portfolios)
        symbols = portfolios.flatMap(_.positions.toList.flatMap(_.toList)).flatMap(_.symbol.toOption).distinct
        quotes <- yfCsvQuoteSvc.getQuotes(cvsParams, symbols: _*)
        mapping = Map(quotes.map(q => q.symbol -> q): _*)

        rankings = portfolios map { portfolio =>
          val player = contest.flatMap(_.participants.toOption).flatMap(_.find(_._id == portfolio.playerID)).orUndefined
          val positions = portfolio.positions.toList.flatMap(_.toList)
          val totalInvestment = computeInvestment(positions, mapping)
          console.log(s"portfolio = %j", portfolio)

          val startingBalance = contest.flatMap(_.startingBalance.toOption).orUndefined
          val funds = portfolio.cashAccount.flatMap(_.funds)
          val totalEquity = funds.map(_ + totalInvestment)
          val gainLoss_% = for (bal <- startingBalance; equity <- totalEquity) yield 100.0 * ((equity - bal) / bal)

          new Participant(
            _id = portfolio.playerID,
            facebookID = player.flatMap(_.facebookID),
            name = player.flatMap(_.name),
            rank = js.undefined,
            totalEquity = totalEquity,
            gainLoss = gainLoss_%)
        }

        // sort the rankings and add the position (e.g. "1st")
        sortedRankings = {
          val myRankings = rankings.sortBy(-_.gainLoss.getOrElse(0.0))
          myRankings.zipWithIndex foreach { case (ranking, index) =>
            ranking.rank = (index + 1) nth
          }
          js.Array(myRankings: _*)
        }

      } yield sortedRankings

      outcome onComplete {
        case Success(rankings) => response.send(rankings); next()
        case Failure(e) =>
          response.internalServerError(e);next()
      }
    }

    /**
      * Retrieves the total investment amount for a specific player
      */
    def totalInvestment(request: Request, response: Response, next: NextFunction): Unit = {
      val playerID = request.params.apply("playerID")
      portfolioDAO.flatMap(_.totalInvestment(playerID)) onComplete {
        case Success(investment) => response.send(new TotalInvestment(investment)); next()
        case Failure(e) => response.internalServerError(e); next()
      }
    }

    /**
      * Transfers funds between accounts; from cash to margin or from margin to cash
      */
    def transferFunds(request: Request, response: Response, next: NextFunction): Unit = {
      val portfolioID = request.params.apply("portfolioID")
      val form = request.bodyAs[FundsTransferRequest]
      val outcome = form.extract match {
        case Some(("cash", amount)) => portfolioDAO.flatMap(_.transferCashFunds(portfolioID, amount = amount))
        case Some(("margin", amount)) => portfolioDAO.flatMap(_.transferMarginFunds(portfolioID, amount = amount))
        case Some((accountType, _)) => die(s"Invalid account type '$accountType'")
        case None => die("Missing account type")
      }
      outcome onComplete {
        case Success(result) if result.isOk => response.send(result.value); next()
        case Success(result) => response.notFound(portfolioID); next()
        case Failure(e: IllegalStateException) => response.badRequest(e.getMessage); next()
        case Failure(e) => response.internalServerError(e); next()
      }
    }

  }

  /**
    * Computes the total investment amount for a specific player
    * @param positions the given collection of positions
    * @param mapping   the symbol to quotes mapping
    * @return the player's total investment
    */
  private def computeInvestment(positions: Seq[PositionLike], mapping: Map[String, YFCSVQuote]): Double = {
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
    * Represents pricing data
    * @param symbol    the given securities symbol
    * @param quantity  the quantity owned
    * @param pricePaid the price paid per share
    */
  case class PricingData(symbol: String, quantity: Double, pricePaid: Double)

  /**
    * New Order Form Extensions
    * @param form the given [[NewOrderForm form]]
    */
  implicit class NewOrderFormExtensions(val form: NewOrderForm) extends AnyVal {

    @inline
    def toOrder: OrderData = {
      new OrderData(
        _id = js.undefined,
        symbol = form.symbol,
        exchange = form.exchange,
        accountType = form.accountType,
        orderType = form.orderType,
        priceType = form.priceType,
        price = if (form.isLimitOrder) form.limitPrice else js.undefined,
        quantity = form.quantity,
        creationTime = new js.Date() - 1.day, // TODO remove after testing
        expirationTime = form.orderTerm.map(s => new js.Date() + Try(s.toInt).getOrElse(3).days),
        processedTime = js.undefined,
        statusMessage = js.undefined)
    }
  }

}
