package com.shocktrade.webapp.routes

import com.shocktrade.serverside.TradingClock
import org.scalajs.nodejs.NodeRequire
import org.scalajs.nodejs.express.{Application, Request, Response}
import org.scalajs.nodejs.moment._
import org.scalajs.nodejs.moment.timezone._
import org.scalajs.nodejs.mongodb.{Db, MongoDB}

import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.js
import scala.scalajs.js.annotation.ScalaJSDefined

/**
  * Trading Clock Routes
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
object TradingClockRoutes {

  def init(app: Application, dbFuture: Future[Db])(implicit ec: ExecutionContext, require: NodeRequire, mongo: MongoDB) = {
    // load modules
    implicit val moment = Moment()
    implicit val momentTz = MomentTimezone()

    // create the trading clock instance
    val tradingClock = new TradingClock()

    // define the routes
    app.get("/api/tradingClock/status/:lastUpdate", (request: Request, response: Response, next: NextFunction) => status(request, response, next))
    app.get("/api/tradingClock/delayUntilStart", (request: Request, response: Response, next: NextFunction) => delayUntilTradingStart(request, response, next))

    //////////////////////////////////////////////////////////////////////////////////////
    //      API Methods
    //////////////////////////////////////////////////////////////////////////////////////

    /**
      * Returns a trading clock state object
      */
    def status(request: Request, response: Response, next: NextFunction) {
      val lastUpdateTimeMillis = request.params("lastUpdate").toDouble
      val active = tradingClock.isTradingActive(js.Date.now())
      val delay = tradingClock.getDelayUntilTradingStartInMillis
      val start = tradingClock.getTradeStartTime
      val end = tradingClock.getTradeStopTime

      // if the last update time was specified, add the state change indicator
      val stateChanged = (lastUpdateTimeMillis > 0) && (active != tradingClock.isTradingActive(lastUpdateTimeMillis))

      // send the trading status
      try {
        response.send(new TradingStatus(
          stateChanged = stateChanged,
          active = active,
          sysTime = System.currentTimeMillis(),
          delay = delay,
          start = start.getTime(),
          end = end.getTime()
        ))
      } catch {
        case e: Throwable =>
          e.printStackTrace()
          response.internalServerError(e)
      } finally {
        next()
      }
    }

    /**
      * Returns the delay (in milliseconds) until trading starts
      */
    def delayUntilTradingStart(request: Request, response: Response, next: NextFunction) {
      response.send(new TradingStart(delayInMillis = tradingClock.getDelayUntilTradingStartInMillis))
      next()
    }

  }

  @ScalaJSDefined
  class TradingStart(val delayInMillis: Double) extends js.Object

  @ScalaJSDefined
  class TradingStatus(val active: Boolean, val sysTime: Double, val delay: Double, val start: Double, val end: Double, val stateChanged: Boolean) extends js.Object

}
