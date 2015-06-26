package com.shocktrade.javascript.dashboard

import biz.enef.angulate.{ScopeController, named}
import com.ldaniels528.javascript.angularjs.core.{Http, Timeout}
import com.ldaniels528.javascript.angularjs.extensions.Toaster
import com.shocktrade.javascript.MySession
import com.shocktrade.javascript.ScalaJsHelper._

import scala.scalajs.concurrent.JSExecutionContext.Implicits.runNow
import scala.scalajs.js
import scala.scalajs.js.Dynamic.{global => g, literal => JS}
import scala.scalajs.js.JSON
import scala.util.{Failure, Success}

/**
 * Exposure Controller
 * @author lawrence.daniels@gmail.com
 */
class ExposureController($scope: js.Dynamic, $http: Http, $timeout: Timeout, toaster: Toaster,
                         @named("ContestService") contestService: ContestService,
                         @named("MySession") mySession: MySession)
  extends ScopeController {

  private var chartData = emptyArray[js.Dynamic]
  private val colors = js.Array("#00ff00", "#88ffff", "#8888ff", "#ff8000", "#88ffaa", "#ff88ff", "#ff8888")
  private val exposures = js.Array(
    JS(value = "sector", label = "Sector Exposure"),
    JS(value = "industry", label = "Industry Exposure"),
    JS(value = "exchange", label = "Exchange Exposure"),
    JS(value = "market", label = "Exchange Sub-Market Exposure"),
    JS(value = "securities", label = "Securities Exposure"))

  ///////////////////////////////////////////////////////////////////////////
  //          Public Variables
  ///////////////////////////////////////////////////////////////////////////

  $scope.selectedExposure = null

  ///////////////////////////////////////////////////////////////////////////
  //          Public Functions
  ///////////////////////////////////////////////////////////////////////////

  $scope.getExposures = () => exposures

  $scope.getChartData = () => chartData

  $scope.exposurePieChart = (exposure: js.UndefOr[String], contestID: js.UndefOr[String], userID: js.UndefOr[String]) => {
    exposurePieChart(exposure, contestID, userID)
  }

  $scope.colorFunction = () => (d: js.Dynamic, i: Double) => colors(i.toInt % colors.length)

  $scope.xFunction = () => { (d: js.Dynamic) => d.label }: js.Function1[js.Dynamic, js.Dynamic]

  $scope.yFunction = () => { (d: js.Dynamic) => d.value }: js.Function1[js.Dynamic, js.Dynamic]

  ///////////////////////////////////////////////////////////////////////////
  //          Private Functions
  ///////////////////////////////////////////////////////////////////////////

  private def exposurePieChart(exposure: js.UndefOr[String], contestID: js.UndefOr[String], userID: js.UndefOr[String]) = {
    for {
      exp <- exposure.toOption
      cid <- contestID.toOption
      uid <- userID.toOption
    } {
      contestService.getExposureChartData(exp, cid, uid) onComplete {
        case Success(data) =>
          chartData = data
        case Failure(e) =>
          g.console.error(s"Failed to load ${JSON.stringify(exposure)} data")
      }
    }
  }

}
