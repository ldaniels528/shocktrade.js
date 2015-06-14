package com.shocktrade.javascript.discover

import biz.enef.angulate.Service
import com.greencatsoft.angularjs.core.HttpService
import com.shocktrade.javascript.ScalaJsHelper._
import com.shocktrade.javascript.discover.MarketStatusService.MarketStatus
import prickle.Unpickle

import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.js
import scala.scalajs.js.JSON

/**
 * Market Status Service
 * @author lawrence.daniels@gmail.com
 */
class MarketStatusService($http: HttpService) extends Service {

  /**
   * Retrieves the current stock market status
   * @return the current U.S. Stock [[MarketStatus market status]]
   * @example {"stateChanged":false,"active":false,"sysTime":1392092448795,"delay":-49848795,"start":1392042600000,"end":1392066000000}
   */
  def getMarketStatus(implicit ec: ExecutionContext): Future[MarketStatus] = flatten {
    val task: Future[js.Any] = $http.get("/api/tradingClock/status/0")
    task
      .map(JSON.stringify(_))
      .map(Unpickle[MarketStatus].fromString(_))
  }

}

/**
 * Market Status Service Singleton
 * @author lawrence.daniels@gmail.com
 */
object MarketStatusService {

  case class MarketStatus(stateChanged: Boolean, active: Boolean, sysTime: Double, delay: Double, start: Double, end: Double)

}