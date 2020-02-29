package com.shocktrade.qualification.routes

import com.shocktrade.qualification.OrderQualificationEngine
import com.shocktrade.server.common.TradingClock
import io.scalajs.npm.express.{Application, Request, Response}
import io.scalajs.npm.mongodb.Db

import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.js
import scala.scalajs.js.JSConverters._
import scala.util.{Failure, Success}

/**
  * Qualification Routes
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
object QualificationRoutes {

  def init[T](app: Application, qualificationEngine: OrderQualificationEngine)(implicit ec: ExecutionContext, dbFuture: Future[Db], clock: TradingClock) = {
    app.get("/api/qualify", (request: Request, response: Response, next: NextFunction) => qualify(request, response, next))
    app.get("/api/qualify/:pid", (request: Request, response: Response, next: NextFunction) => processOrders(request, response, next))
    app.get("/api/qualify/:pid/orders", (request: Request, response: Response, next: NextFunction) => qualifyingOrders(request, response, next))

    //////////////////////////////////////////////////////////////////////////////////////
    //      API Methods
    //////////////////////////////////////////////////////////////////////////////////////

    def qualify(request: Request, response: Response, next: NextFunction) = {
      val marketClosed = request.query.get("marketClosed").contains("true")
      qualificationEngine.qualifyAll(marketClosed) onComplete {
        case Success((claims, startTime, processedTime)) =>
          response.send(new CycleResponse(claims.toJSArray, processedTime))
          next()
        case Failure(e) =>
          e.printStackTrace()
          response.internalServerError(e)
          next()
      }
    }

    def processOrders(request: Request, response: Response, next: NextFunction) = {
      val portfolioID = request.params("pid")
      val marketClosed = request.query.get("marketClosed").contains("true")
      /*
      qualificationEngine.processOrderByPID(portfolioID, marketClosed) onComplete {
        case Success(results) => response.send(results.toJSArray); next()
        case Failure(e) =>
          e.printStackTrace()
          response.internalServerError(e)
          next()
      }*/
    }

    def qualifyingOrders(request: Request, response: Response, next: NextFunction) = {
      val portfolioID = request.params("pid")
      val marketClosed = request.query.get("marketClosed").contains("true")
      /*
      qualificationEngine.getQualifyingOrdersByPID(portfolioID, marketClosed) onComplete {
        case Success(orders) => response.send(orders.toJSArray); next()
        case Failure(e) =>
          e.printStackTrace()
          response.internalServerError(e)
          next()
      }*/
    }

  }

  class CycleResponse(val results: js.Array[_], val processedTime: Double) extends js.Object

}
