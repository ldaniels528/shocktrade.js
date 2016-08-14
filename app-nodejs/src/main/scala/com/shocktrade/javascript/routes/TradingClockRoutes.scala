package com.shocktrade.javascript.routes

import com.shocktrade.javascript.util.{DateUtil, TradingClock}
import org.scalajs.nodejs.express.{Application, Request, Response}
import org.scalajs.nodejs.mongodb.{Db, MongoDB}

import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.js
import scala.scalajs.js.annotation.ScalaJSDefined

/**
  * Trading Clock Routes
  * @author lawrence.daniels@gmail.com
  */
object TradingClockRoutes {

  def init(app: Application, dbFuture: Future[Db])(implicit ec: ExecutionContext, mongo: MongoDB) = {
    app.get("/api/tradingClock/status/:lastUpdate", (request: Request, response: Response, next: NextFunction) => status(request, response, next))
    app.get("/api/tradingClock/delayUntilStart", (request: Request, response: Response, next: NextFunction) => delayUntilTradingStart(request, response, next))
  }

  /**
    * Returns a trading clock state object
    */
  def status(request: Request, response: Response, next: NextFunction) {
    val lastUpdateTimeMillis = request.params("lastUpdate").toDouble
    val active = DateUtil.isTradingActive(System.currentTimeMillis())
    val delay = DateUtil.getDelayUntilTradingStartInMillis
    val start = DateUtil.getTradeStartTime
    val end = DateUtil.getTradeStopTime
    var stateChanged = false

    // if the last update time was specified, add the state change indicator
    if (lastUpdateTimeMillis > 0) {
      stateChanged = active != TradingClock.isTradingActive(lastUpdateTimeMillis)
    }

    // send the trading status
    response.send(new TradingStatus(
      stateChanged = stateChanged,
      active = active,
      sysTime = System.currentTimeMillis(),
      delay = delay,
      start = start.getTime(),
      end = end.getTime()
    ))
    next()
  }

  /**
    * Returns the delay (in milliseconds) until trading starts
    */
  def delayUntilTradingStart(request: Request, response: Response, next: NextFunction) {
    response.send(new TradingStart(delayInMillis = TradingClock.getDelayUntilTradingStartInMillis))
    next()
  }

  @ScalaJSDefined
  class TradingStart(val delayInMillis: Double) extends js.Object

  @ScalaJSDefined
  class TradingStatus(val active: Boolean, val sysTime: Double, val delay: Double, val start: Double, val end: Double, val stateChanged: Boolean) extends js.Object

}
