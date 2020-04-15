package com.shocktrade.webapp.routes.contest

import com.shocktrade.common.Ok
import com.shocktrade.common.api.PortfolioAPI
import com.shocktrade.common.forms.NewOrderForm
import com.shocktrade.webapp.routes._
import com.shocktrade.webapp.routes.contest.PortfolioHelper._
import com.shocktrade.webapp.routes.contest.dao._
import io.scalajs.nodejs.console
import io.scalajs.npm.express.{Application, Request, Response}

import scala.concurrent.ExecutionContext
import scala.scalajs.js
import scala.util.{Failure, Success}

/**
 * Portfolio Routes
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
class PortfolioRoutes(app: Application)(implicit ec: ExecutionContext, portfolioDAO: PortfolioDAO) extends PortfolioAPI {
  // individual objects
  app.get(findPortfolioByIDURL(":portfolioID"), (request: Request, response: Response, next: NextFunction) => findPortfolioByID(request, response, next))
  app.get(findHeldSecuritiesURL(":portfolioID"), (request: Request, response: Response, next: NextFunction) => findHeldSecurities(request, response, next))
  app.get(findChartURL(":id", ":userID", ":chart"), (request: Request, response: Response, next: NextFunction) => findChart(request, response, next))

  // perks
  app.get(findAvailablePerksURL, (request: Request, response: Response, next: NextFunction) => findAvailablePerks(request, response, next))
  app.get(findPurchasedPerksURL(":portfolioID"), (request: Request, response: Response, next: NextFunction) => findPurchasedPerks(request, response, next))
  app.post(purchasePerksURL(":portfolioID"), (request: Request, response: Response, next: NextFunction) => purchasePerks(request, response, next))

  // positions
  app.get(findPositionsURL(":contestID", ":userID"), (request: Request, response: Response, next: NextFunction) => findPositions(request, response, next))

  // orders
  app.delete(cancelOrderURL(":portfolioID", ":orderID"), (request: Request, response: Response, next: NextFunction) => cancelOrder(request, response, next))
  app.get(findOrdersURL(":contestID", ":userID"), (request: Request, response: Response, next: NextFunction) => findOrders(request, response, next))
  app.post(createOrderByIDURL(":portfolioID"), (request: Request, response: Response, next: NextFunction) => createOrderByID(request, response, next))
  app.post(createOrderURL(":contestID", ":userID"), (request: Request, response: Response, next: NextFunction) => createOrder(request, response, next))

  // portfolios
  app.get(findPortfolioByUserURL(":contestID", ":userID"), (request: Request, response: Response, next: NextFunction) => findPortfolioByUser(request, response, next))
  app.get(findPortfolioBalanceURL(":contestID", ":userID"), (request: Request, response: Response, next: NextFunction) => findPortfolioBalance(request, response, next))
  app.get(findPortfoliosByContestURL(":contestID"), (request: Request, response: Response, next: NextFunction) => findPortfoliosByContest(request, response, next))
  app.get(findPortfoliosByUserURL(":userID"), (request: Request, response: Response, next: NextFunction) => findPortfoliosByUser(request, response, next))

  //////////////////////////////////////////////////////////////////////////////////////
  //      API Methods
  //////////////////////////////////////////////////////////////////////////////////////

  def createOrder(request: Request, response: Response, next: NextFunction): Unit = {
    val (contestID, userID) = (request.params("contestID"), request.params("userID"))
    val form = request.bodyAs[NewOrderForm]
    form.validate match {
      case messages if messages.nonEmpty => response.badRequest(messages)
      case _ =>
        portfolioDAO.createOrder(contestID, userID, order = form.toOrder) onComplete {
          case Success(count) => response.send(Ok(count)); next()
          case Failure(e) => response.showException(e).internalServerError(e); next()
        }
    }
  }

  /**
   * Creates a new order within a portfolio by portfolio ID
   */
  def createOrderByID(request: Request, response: Response, next: NextFunction): Unit = {
    val portfolioID = request.params("portfolioID")
    val form = request.bodyAs[NewOrderForm]
    form.validate match {
      case messages if messages.nonEmpty => response.badRequest(messages); next()
      case _ =>
        val order = form.toOrder
        portfolioDAO.createOrder(portfolioID, order) onComplete {
          case Success(count) if count == 1 => response.send(new Ok(count)); next()
          case Success(count) =>
            console.error(s"failed result = $count")
            response.notFound(s"Order for portfolio # $portfolioID")
            next()
          case Failure(e) => response.internalServerError(e); next()
        }
    }
  }

  def findChart(request: Request, response: Response, next: NextFunction): Unit = {
    val (contestID, userID, chart) = (request.params("id"), request.params("userID"), request.params("chart"))
    portfolioDAO.findChartData(contestID, userID, chart) onComplete {
      case Success(data) => response.send(data); next()
      case Failure(e) => response.showException(e).internalServerError(e); next()
    }
  }

  /**
   * Returns the symbols for securities currently held in all active portfolios for a given player by player ID
   */
  def findHeldSecurities(request: Request, response: Response, next: NextFunction): Unit = {
    val portfolioID = request.params("portfolioID")
    portfolioDAO.findHeldSecurities(portfolioID) onComplete {
      case Success(tickers) => response.send(tickers); next()
      case Failure(e) => response.internalServerError(e); next()
    }
  }

  /**
   * Cancels an order by portfolio and order IDs
   */
  def cancelOrder(request: Request, response: Response, next: NextFunction): Unit = {
    val orderID = request.params("orderID")
    portfolioDAO.cancelOrder(orderID) onComplete {
      case Success(count) if count == 1 => response.send(Ok(count)); next()
      case Success(count) =>
        console.error(s"update result = $count")
        response.notFound(request.params)
        next()
      case Failure(e) => response.internalServerError(e); next()
    }
  }

  /**
   * Retrieves all orders by portfolio ID
   */
  def findOrders(request: Request, response: Response, next: NextFunction): Unit = {
    val (contestID, userID) = (request.params("contestID"), request.params("userID"))
    portfolioDAO.findOrders(contestID, userID) onComplete {
      case Success(orders) => response.send(orders); next()
      case Failure(e) => response.internalServerError(e); next()
    }
  }

  def findPortfolioByUser(request: Request, response: Response, next: NextFunction): Unit = {
    val (contestID, userID) = (request.params("contestID"), request.params("userID"))
    portfolioDAO.findPortfolioByUser(contestID, userID) onComplete {
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
   * Retrieves available perks
   */
  def findAvailablePerks(request: Request, response: Response, next: NextFunction): Unit = {
    portfolioDAO.findAvailablePerks onComplete {
      case Success(perks) => response.send(perks); next()
      case Failure(e) => response.internalServerError(e); next()
    }
  }

  /**
   * Retrieves the purchased perks by portfolio ID
   */
  def findPurchasedPerks(request: Request, response: Response, next: NextFunction): Unit = {
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
      perks <- portfolioDAO.findAvailablePerks
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
  def findPositions(request: Request, response: Response, next: NextFunction): Unit = {
    val (contestID, userID) = (request.params("contestID"), request.params("userID"))
    portfolioDAO.findPositions(contestID, userID) onComplete {
      case Success(positions) => response.send(positions); next()
      case Failure(e) => response.internalServerError(e); next()
    }
  }

  /**
   * Retrieves a portfolio by a contest ID and user ID
   */
  def findPortfolioByID(request: Request, response: Response, next: NextFunction): Unit = {
    val portfolioID = request.params("portfolioID")
    portfolioDAO.findPortfolioByID(portfolioID) onComplete {
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
    portfolioDAO.findPortfoliosByContest(contestID) onComplete {
      case Success(portfolios) => response.send(portfolios); next()
      case Failure(e) => response.internalServerError(e); next()
    }
  }

  /**
   * Retrieves a collection of portfolios by a user ID
   */
  def findPortfoliosByUser(request: Request, response: Response, next: NextFunction): Unit = {
    val userID = request.params("userID")
    portfolioDAO.findPortfoliosByUser(userID) onComplete {
      case Success(portfolios) => response.send(portfolios); next()
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

}
