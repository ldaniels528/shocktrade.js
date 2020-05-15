package com.shocktrade.webapp.routes.discover

import com.shocktrade.server.common.TradingClock
import com.shocktrade.webapp.routes._
import com.shocktrade.webapp.routes.contest.dao.ContestDAO
import com.shocktrade.webapp.routes.discover.TradingClockRoutes._
import io.scalajs.npm.express.{Application, Request, Response}
import io.scalajs.util.JsUnderOrHelper._

import scala.concurrent.ExecutionContext
import scala.scalajs.js
import scala.util.{Failure, Success}

/**
 * Trading Clock Routes
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
class TradingClockRoutes(app: Application)(implicit ec: ExecutionContext, tradingClock: TradingClock, contestDAO: ContestDAO) {
  // define the routes
  app.get("/api/tradingClock/status/:lastUpdate", (request: Request, response: Response, next: NextFunction) => status(request, response, next))
  app.get("/api/tradingClock/:contestID/status/:lastUpdate", (request: Request, response: Response, next: NextFunction) => contestStatus(request, response, next))
  app.get("/api/tradingClock/delayUntilStart", (request: Request, response: Response, next: NextFunction) => delayUntilTradingStart(request, response, next))

  //////////////////////////////////////////////////////////////////////////////////////
  //      API Methods
  //////////////////////////////////////////////////////////////////////////////////////

  /**
   * Returns a trading clock state object
   */
  def contestStatus(request: Request, response: Response, next: NextFunction): Unit = {
    val (contestID, lastUpdateTimeMillis) = (request.params("contestID"), request.params("lastUpdate").toDouble)

    // lookup the contest and use its time offset
    contestDAO.findOneByID(contestID) onComplete {
      case Success(Some(contest)) =>
        val timeOffset = contest.timeOffset.orZero
        val now = js.Date.now() - timeOffset
        val active = tradingClock.isTradingActive(now)

        // send the response
        response.send(new TradingStatus(
          contestID = contestID,
          stateChanged = (lastUpdateTimeMillis > 0) && (active != tradingClock.isTradingActive(lastUpdateTimeMillis)),
          active = true, // TODO active,
          sysTime = System.currentTimeMillis(),
          delay = tradingClock.getDelayUntilTradingStartInMillis - timeOffset,
          start = tradingClock.getTradeStartTime.getTime() - timeOffset,
          end = tradingClock.getTradeStopTime.getTime() - timeOffset
        ))
        next()
      case Success(None) => response.notFound(request.params); next()
      case Failure(e) => response.showException(e).internalServerError(e); next()
    }
  }

  /**
   * Returns a trading clock state object
   */
  def status(request: Request, response: Response, next: NextFunction): Unit = {
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
        response.showException(e).internalServerError(e)
    } finally {
      next()
    }
  }

  /**
   * Returns the delay (in milliseconds) until trading starts
   */
  def delayUntilTradingStart(request: Request, response: Response, next: NextFunction): Unit = {
    response.send(new TradingStart(delayInMillis = tradingClock.getDelayUntilTradingStartInMillis))
    next()
  }

}

/**
 * Trading Clock Routes
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
object TradingClockRoutes {

  class TradingStart(val delayInMillis: Double) extends js.Object

  class TradingStatus(val contestID: js.UndefOr[String] = js.undefined,
                      val active: Boolean,
                      val sysTime: Double,
                      val delay: Double,
                      val start: Double,
                      val end: Double,
                      val stateChanged: Boolean) extends js.Object

}
