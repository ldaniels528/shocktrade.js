package com.shocktrade.javascript.discover

import biz.enef.angulate._
import biz.enef.angulate.core.HttpService

import scala.scalajs.js
import scala.scalajs.js.annotation.JSExportAll

/**
 * Market Status Service
 * @author lawrence.daniels@gmail.com
 */
@JSExportAll
class MarketStatusService($http: HttpService) extends Service {

  /**
   * Retrieves the current stock market status
   * @return the current stock market status
   * @example {"stateChanged":false,"active":false,"sysTime":1392092448795,"delay":-49848795,"start":1392042600000,"end":1392066000000}
   */
  def getMarketStatus: js.Function = () => $http.get[js.Dynamic]("/api/tradingClock/status/0")

}
