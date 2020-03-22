package com.shocktrade.webapp.routes.contest

import com.shocktrade.common.Ok
import com.shocktrade.common.forms.NewOrderForm
import com.shocktrade.common.models.contest.{MarketValueResponse, TotalInvestment}
import com.shocktrade.webapp.routes.NextFunction
import io.scalajs.npm.express.{Application, Request, Response}
import io.scalajs.util.DateHelper._

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import scala.scalajs.js
import scala.util.{Failure, Success, Try}

/**
 * Portfolio Routes
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
class PortfolioRoutes(app: Application)(implicit ec: ExecutionContext) {
  private val contestDAO = ContestDAO()
  private val orderDAO = OrderDAO()
  private val perkDAO = PerksDAO()
  private val portfolioDAO = PortfolioDAO()
  private val positionDAO = PositionDAO()

  // individual objects
  app.get("/api/portfolio/:portfolioID", (request: Request, response: Response, next: NextFunction) => findPortfolioByUser(request, response, next))
  app.get("/api/portfolio/:portfolioID/marketValue", (request: Request, response: Response, next: NextFunction) => computeMarketValue(request, response, next))
  app.get("/api/orders/:contestID/user/:userID", (request: Request, response: Response, next: NextFunction) => findOrders(request, response, next))
  app.get("/api/portfolio/:portfolioID/perks", (request: Request, response: Response, next: NextFunction) => findPerksByID(request, response, next))
  app.post("/api/portfolio/:portfolioID/perks", (request: Request, response: Response, next: NextFunction) => purchasePerks(request, response, next))
  app.get("/api/portfolio/:portfolioID/positions", (request: Request, response: Response, next: NextFunction) => findPositionsByID(request, response, next))
  app.get("/api/portfolio/:portfolioID/heldSecurities", (request: Request, response: Response, next: NextFunction) => findHeldSecurities(request, response, next))

  // individual objects by reference
  app.get("/api/portfolio/contest/:contestID/user/:userID", (request: Request, response: Response, next: NextFunction) => findPortfolio(request, response, next))
  app.get("/api/portfolio/contest/:contestID/user/:userID/balance", (request: Request, response: Response, next: NextFunction) => findPortfolioBalance(request, response, next))

  // collections
  app.get("/api/portfolios/contest/:contestID", (request: Request, response: Response, next: NextFunction) => findPortfoliosByContest(request, response, next))
  app.get("/api/portfolios/user/:userID", (request: Request, response: Response, next: NextFunction) => findPortfoliosByUser(request, response, next))
  app.get("/api/portfolios/:portfolioID/totalInvestment", (request: Request, response: Response, next: NextFunction) => findTotalInvestment(request, response, next))

  //////////////////////////////////////////////////////////////////////////////////////
  //      API Methods
  //////////////////////////////////////////////////////////////////////////////////////

  /**
   * Computes the market value of an account by account type and portfolio ID
   */
  def computeMarketValue(request: Request, response: Response, next: NextFunction): Unit = {
    val portfolioID = request.params("portfolioID")
    portfolioDAO.computeMarketValue(portfolioID) onComplete {
      case Success(marketValue) => response.send(new MarketValueResponse(marketValue)); next()
      case Failure(e: IllegalStateException) => response.badRequest(e.getMessage); next()
      case Failure(e) => response.internalServerError(e); next()
    }
  }

  /**
   * Returns the symbols for securities currently held in all active portfolios for a given player by player ID
   */
  def findHeldSecurities(request: Request, response: Response, next: NextFunction): Unit = {
    val portfolioID = request.params("portfolioID")
    portfolioDAO.findHeldSecurities(portfolioID) onComplete {
      case Success(symbols) => response.send(symbols); next()
      case Failure(e) => response.internalServerError(e); next()
    }
  }

  /**
   * Retrieves all orders by portfolio ID
   */
  def findOrders(request: Request, response: Response, next: NextFunction): Unit = {
    val (contestID, userID) = (request.params("contestID"), request.params("userID"))
    orderDAO.findOrders(contestID, userID) onComplete {
      case Success(orders) => response.send(orders); next()
      case Failure(e) => response.internalServerError(e); next()
    }
  }

  def findPortfolio(request: Request, response: Response, next: NextFunction): Unit = {
    val (contestID, userID) = (request.params("contestID"), request.params("userID"))
    portfolioDAO.findPortfolio(contestID, userID) onComplete {
      case Success(Some(portfolio)) => response.send(portfolio); next()
      case Success(None) => response.notFound(request.params); next()
      case Failure(e) => response.internalServerError(e); next()
    }
  }

  def findPortfolioBalance(request: Request, response: Response, next: NextFunction): Unit = {
    val (contestID, userID) = (request.params("contestID"), request.params("userID"))
    portfolioDAO.findPortfolioBalance(contestID, userID) onComplete {
      case Success(Some(balance)) => response.send(balance); next()
      case Success(None) => response.notFound(request.params); next()
      case Failure(e) => response.internalServerError(e); next()
    }
  }

  /**
   * Retrieves the purchased perks by portfolio ID
   */
  def findPerksByID(request: Request, response: Response, next: NextFunction): Unit = {
    val portfolioID = request.params("portfolioID")
    portfolioDAO.findPurchasedPerks(portfolioID) onComplete {
      case Success(perks) => response.send(perks); next()
      case Failure(e) => response.internalServerError(e); next()
    }
  }

  /**
   * Purchases perks via an array of perk codes and a portfolio ID
   */
  def purchasePerks(request: Request, response: Response, next: NextFunction): Unit = {
    val portfolioID = request.params("portfolioID")
    val purchasePerkCodes = request.bodyAs[js.Array[String]]
    val outcome = for {
      perks <- perkDAO.findAvailablePerks
      perkMapping = js.Dictionary(perks.map(p => p.code.orNull -> p): _*)
      perksCost = (purchasePerkCodes flatMap perkMapping.get).flatMap(_.cost.toOption).sum
      count <- portfolioDAO.purchasePerks(portfolioID, purchasePerkCodes, perksCost) if count >= 1
    } yield count

    outcome onComplete {
      case Success(updated) => response.send(new Ok(updated)); next()
      case Failure(e) => response.internalServerError(e); next()
    }
  }

  /**
   * Retrieves all positions by portfolio ID
   */
  def findPositionsByID(request: Request, response: Response, next: NextFunction): Unit = {
    val portfolioID = request.params("portfolioID")
    positionDAO.findPositions(portfolioID) onComplete {
      case Success(positions) => response.send(positions); next()
      case Failure(e) => response.internalServerError(e); next()
    }
  }

  /**
   * Retrieves a portfolio by a contest ID and user ID
   */
  def findPortfolioByUser(request: Request, response: Response, next: NextFunction): Unit = {
    val userID = request.params("userID")
    portfolioDAO.findOneByUser(userID) onComplete {
      case Success(Some(portfolio)) => response.send(portfolio); next()
      case Success(None) => response.notFound(request.params); next()
      case Failure(e) => response.internalServerError(e); next()
    }
  }

  /**
   * Retrieves a specific portfolio by a contest and user IDs
   */
  def findPortfoliosByContest(request: Request, response: Response, next: NextFunction): Unit = {
    val contestID = request.params("contestID")
    portfolioDAO.findByContest(contestID) onComplete {
      case Success(portfolios) => response.send(portfolios); next()
      case Failure(e) => response.internalServerError(e); next()
    }
  }

  /**
   * Retrieves a collection of portfolios by a user ID
   */
  def findPortfoliosByUser(request: Request, response: Response, next: NextFunction): Unit = {
    val userID = request.params("userID")
    portfolioDAO.findByUser(userID) onComplete {
      case Success(portfolios) => response.send(portfolios); next()
      case Failure(e) => response.internalServerError(e); next()
    }
  }

  /**
   * Retrieves the total investment amount for a specific player
   */
  def findTotalInvestment(request: Request, response: Response, next: NextFunction): Unit = {
    val portfolioID = request.params("portfolioID")
    portfolioDAO.computeTotalInvestment(portfolioID) onComplete {
      case Success(investment) => response.send(new TotalInvestment(investment)); next()
      case Failure(e) => response.internalServerError(e); next()
    }
  }

}

/**
 * Portfolio Routes Companion
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
object PortfolioRoutes {

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
        orderID = js.undefined,
        symbol = form.symbol,
        exchange = form.exchange,
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
