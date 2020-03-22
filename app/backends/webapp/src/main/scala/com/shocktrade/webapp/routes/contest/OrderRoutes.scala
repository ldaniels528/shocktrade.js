package com.shocktrade.webapp.routes.contest

import com.shocktrade.common.Ok
import com.shocktrade.common.forms.NewOrderForm
import com.shocktrade.webapp.routes.contest.PortfolioRoutes._
import com.shocktrade.webapp.routes.NextFunction
import io.scalajs.nodejs.console
import io.scalajs.npm.express.{Application, Request, Response}

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

/**
 * Order Routes
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
class OrderRoutes(app: Application)(implicit ec: ExecutionContext) {
  private val orderDAO = OrderDAO()

  app.delete("/api/order/:orderID", (request: Request, response: Response, next: NextFunction) => cancelOrder(request, response, next))
  app.post("/api/order/:portfolioID", (request: Request, response: Response, next: NextFunction) => createOrder(request, response, next))

  //////////////////////////////////////////////////////////////////////////////////////
  //      API Methods
  //////////////////////////////////////////////////////////////////////////////////////

  /**
   * Cancels an order by portfolio and order IDs
   */
  def cancelOrder(request: Request, response: Response, next: NextFunction): Unit = {
    val orderID = request.params("orderID")
    orderDAO.cancelOrder(orderID) onComplete {
      case Success(count) if count == 1 => response.send(new Ok(count)); next()
      case Success(count) =>
        console.error(s"update result = $count")
        response.notFound(request.params)
        next()
      case Failure(e) => response.internalServerError(e); next()
    }
  }

  /**
   * Creates a new order within a portfolio by portfolio ID
   */
  def createOrder(request: Request, response: Response, next: NextFunction): Unit = {
    val portfolioID = request.params("portfolioID")
    val form = request.bodyAs[NewOrderForm]
    form.validate match {
      case messages if messages.nonEmpty => response.badRequest(messages); next()
      case _ =>
        val order = form.toOrder
        orderDAO.createOrder(portfolioID, order) onComplete {
          case Success(count) if count == 1 => response.send(new Ok(count)); next()
          case Success(count) =>
            console.error(s"failed result = $count")
            response.notFound(s"Order for portfolio # $portfolioID")
            next()
          case Failure(e) => response.internalServerError(e); next()
        }
    }
  }

}
