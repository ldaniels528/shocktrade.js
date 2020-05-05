package com.shocktrade.webapp.routes.contest

import com.shocktrade.common.Ok
import com.shocktrade.common.api.PortfolioAPI
import com.shocktrade.common.forms.NewOrderForm
import com.shocktrade.webapp.routes._
import com.shocktrade.webapp.routes.contest.PortfolioHelper._
import com.shocktrade.webapp.routes.contest.dao._
import com.shocktrade.webapp.vm.VirtualMachine
import com.shocktrade.webapp.vm.dao.VirtualMachineDAO
import com.shocktrade.webapp.vm.opcodes.{CancelOrder, CreateOrder, PurchasePerks}
import io.scalajs.npm.express.{Application, Request, Response}

import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.js
import scala.util.{Failure, Success}

/**
 * Portfolio Routes
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
class PortfolioRoutes(app: Application)(implicit ec: ExecutionContext, portfolioDAO: PortfolioDAO, vmDAO: VirtualMachineDAO, vm: VirtualMachine)
  extends PortfolioAPI {
  // finders
  app.get(findPortfolioByIDURL(":portfolioID"), (request: Request, response: Response, next: NextFunction) => findPortfolioByID(request, response, next))
  app.get(findHeldSecuritiesURL(":portfolioID"), (request: Request, response: Response, next: NextFunction) => findHeldSecurities(request, response, next))
  app.get(findChartURL(":id", ":userID", ":chart"), (request: Request, response: Response, next: NextFunction) => findChart(request, response, next))

  // perks
  app.get(findPurchasedPerksURL(":portfolioID"), (request: Request, response: Response, next: NextFunction) => findPurchasedPerks(request, response, next))
  app.post(purchasePerksURL(":portfolioID"), (request: Request, response: Response, next: NextFunction) => purchasePerks(request, response, next))

  // positions
  app.get(findPositionByIDURL(":positionID"), (request: Request, response: Response, next: NextFunction) => findPositionByID(request, response, next))
  app.get(findPositionsURL(":contestID", ":userID"), (request: Request, response: Response, next: NextFunction) => findPositions(request, response, next))

  // orders
  app.delete(cancelOrderURL(":orderID"), (request: Request, response: Response, next: NextFunction) => cancelOrder(request, response, next))
  app.get(findOrderByIDURL(":orderID"), (request: Request, response: Response, next: NextFunction) => findOrderByID(request, response, next))
  app.get(findOrdersURL(":contestID", ":userID"), (request: Request, response: Response, next: NextFunction) => findOrders(request, response, next))
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
      case messages if messages.nonEmpty => response.badRequest(messages); next()
      case _ =>
        val outcome = for {
          portfolioID <- portfolioDAO.findPortfolioIdByUser(contestID, userID) flatMap { portfolioID_? =>
            val result = for {portfolioID <- portfolioID_?} yield portfolioID
            result match {
              case Some(portfolioID) => Future.successful(portfolioID)
              case None => Future.failed(js.JavaScriptException(s"Portfolio for user $userID, contest $contestID"))
            }
          }
          result <- vm.invoke(CreateOrder(portfolioID, form.toOrder))
        } yield result

        outcome onComplete {
          case Success(result) => response.send(result); next()
          case Failure(e) => response.showException(e).internalServerError(e); next()
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
      case Failure(e) => response.showException(e).internalServerError(e); next()
    }
  }

  /**
   * Cancels an order by portfolio and order IDs
   */
  def cancelOrder(request: Request, response: Response, next: NextFunction): Unit = {
    val orderID = request.params("orderID")
    vm.invoke(CancelOrder(orderID)) onComplete {
      case Success(result) => response.send(result); next()
      case Failure(e) => response.showException(e).internalServerError(e); next()
    }
  }

  /**
   * Retrieves an order by ID
   */
  def findOrderByID(request: Request, response: Response, next: NextFunction): Unit = {
    val orderID = request.params("orderID")
    portfolioDAO.findOrderByID(orderID) onComplete {
      case Success(Some(order)) => response.send(order); next()
      case Success(None) => response.notFound(request.params); next()
      case Failure(e) => response.showException(e).internalServerError(e); next()
    }
  }

  /**
   * Retrieves all orders by portfolio ID
   */
  def findOrders(request: Request, response: Response, next: NextFunction): Unit = {
    val (contestID, userID) = (request.params("contestID"), request.params("userID"))
    portfolioDAO.findOrders(contestID, userID) onComplete {
      case Success(orders) => response.send(orders); next()
      case Failure(e) => response.showException(e).internalServerError(e); next()
    }
  }

  def findPortfolioByUser(request: Request, response: Response, next: NextFunction): Unit = {
    val (contestID, userID) = (request.params("contestID"), request.params("userID"))
    portfolioDAO.findPortfolioByUser(contestID, userID) onComplete {
      case Success(Some(portfolio)) => response.send(portfolio); next()
      case Success(None) => response.notFound(request.params); next()
      case Failure(e) => response.showException(e).internalServerError(e); next()
    }
  }

  def findPortfolioBalance(request: Request, response: Response, next: NextFunction): Unit = {
    val (contestID, userID) = (request.params("contestID"), request.params("userID"))
    portfolioDAO.findPortfolioBalance(contestID, userID) onComplete {
      case Success(Some(balance)) => response.send(balance); next()
      case Success(None) => response.notFound(request.params); next()
      case Failure(e) => response.showException(e).internalServerError(e); next()
    }
  }

  /**
   * Retrieves the purchased perks by portfolio ID
   */
  def findPurchasedPerks(request: Request, response: Response, next: NextFunction): Unit = {
    val portfolioID = request.params("portfolioID")
    portfolioDAO.findPurchasedPerks(portfolioID) onComplete {
      case Success(perks) => response.send(perks); next()
      case Failure(e) => response.showException(e).internalServerError(e); next()
    }
  }

  /**
   * Purchases perks via an array of perk codes and a portfolio ID
   */
  def purchasePerks(request: Request, response: Response, next: NextFunction): Unit = {
    val portfolioID = request.params("portfolioID")
    val perkCodes = request.bodyAs[js.Array[String]]
    vm.invoke(PurchasePerks(portfolioID, perkCodes)) onComplete {
      case Success(result) => response.send(result); next()
      case Failure(e) => response.showException(e).internalServerError(e); next()
    }
  }

  /**
   * Retrieves a position by its ID
   */
  def findPositionByID(request: Request, response: Response, next: NextFunction): Unit = {
    val positionID = request.params("positionID")
    portfolioDAO.findPositionByID(positionID) onComplete {
      case Success(Some(position)) => response.send(position); next()
      case Success(None) => response.notFound(request.params); next()
      case Failure(e) => response.showException(e).internalServerError(e); next()
    }
  }

  /**
   * Retrieves all positions by portfolio ID
   */
  def findPositions(request: Request, response: Response, next: NextFunction): Unit = {
    val (contestID, userID) = (request.params("contestID"), request.params("userID"))
    portfolioDAO.findPositions(contestID, userID) onComplete {
      case Success(positions) => response.send(positions); next()
      case Failure(e) => response.showException(e).internalServerError(e); next()
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
      case Failure(e) => response.showException(e).internalServerError(e); next()
    }
  }

  /**
   * Retrieves a specific portfolio by a contest and user IDs
   */
  def findPortfoliosByContest(request: Request, response: Response, next: NextFunction): Unit = {
    val contestID = request.params("contestID")
    portfolioDAO.findPortfoliosByContest(contestID) onComplete {
      case Success(portfolios) => response.send(portfolios); next()
      case Failure(e) => response.showException(e).internalServerError(e); next()
    }
  }

  /**
   * Retrieves a collection of portfolios by a user ID
   */
  def findPortfoliosByUser(request: Request, response: Response, next: NextFunction): Unit = {
    val userID = request.params("userID")
    portfolioDAO.findPortfoliosByUser(userID) onComplete {
      case Success(portfolios) => response.send(portfolios); next()
      case Failure(e) => response.showException(e).internalServerError(e); next()
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
