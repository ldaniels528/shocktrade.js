package com.shocktrade.client.discover

import io.scalajs.npm.angularjs.Service
import io.scalajs.npm.angularjs.http.{Http, HttpResponse}

import scala.scalajs.js

/**
 * Market Status Service
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
class MarketStatusService($http: Http) extends Service {

  /**
   * Retrieves the current stock market status
   * @return the current U.S. Stock [[MarketStatus market status]]
   */
  def getMarketStatus: js.Promise[HttpResponse[MarketStatus]] = $http.get("/api/tradingClock/status/0")

  /**
   * Retrieves the current stock market status
   * @param contestID the given contest ID
   * @return the current U.S. Stock [[MarketStatus market status]]
   */
  def getMarketStatus(contestID: String): js.Promise[HttpResponse[MarketStatus]] = $http.get(s"/api/tradingClock/$contestID/status/0")

}

/**
 * Represents the current U.S. Market Status
 * @example {"stateChanged":false,"active":false,"sysTime":1392092448795,"delay":-49848795,"start":1392042600000,"end":1392066000000}
 */
@js.native
trait MarketStatus extends js.Object {
  var stateChanged: Boolean = js.native
  var active: Boolean = js.native
  var sysTime: Double = js.native
  var delay: Double = js.native
  var start: Double = js.native
  var end: Double = js.native
}
