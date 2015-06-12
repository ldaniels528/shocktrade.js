package com.shocktrade.javascript.dashboard

import biz.enef.angulate.core.{HttpService, Timeout}
import biz.enef.angulate.{ScopeController, named}
import com.ldaniels528.angularjs.Toaster
import com.shocktrade.javascript.MySession
import com.shocktrade.javascript.ScalaJsHelper._

import scala.scalajs.js
import scala.scalajs.js.Dynamic.{global => g, literal => JS}
import scala.scalajs.js.JSON
import scala.util.{Failure, Success}

/**
 * Exposure Controller
 * @author lawrence.daniels@gmail.com
 */
class ExposureController($scope: js.Dynamic, $http: HttpService, $timeout: Timeout, toaster: Toaster,
                         @named("ContestService") contestService: ContestService,
                         @named("MySession") mySession: MySession) extends ScopeController {

  private var chartData = emptyArray[js.Dynamic]
  private val colors = js.Array("#00ff00", "#88ffff", "#8888ff", "#ff8000", "#88ffaa", "#ff88ff", "#ff8888")
  private val exposures = js.Array(
    JS(value = "sector", label = "Sector Exposure"),
    JS(value = "industry", label = "Industry Exposure"),
    JS(value = "exchange", label = "Exchange Exposure"),
    JS(value = "market", label = "Exchange Sub-Market Exposure"),
    JS(value = "securities", label = "Securities Exposure"))

  /**
   * Initializes the view by displaying an initial chart
   */
  $scope.init = () => init()

  $scope.selectedExposure = exposures.last

  $scope.getExposures = () => exposures

  $scope.getChartData = () => chartData

  $scope.exposurePieChart = (contest: js.Dynamic, exposure: js.Dynamic, userID: js.Dynamic) => {
    g.console.log(s"contest = $contest, exposure = $exposure, userID = $userID")
    $http.get[js.Array[js.Dynamic]](s"/api/charts/exposure/$exposure/${contest.OID}/$userID") onComplete {
      case Success(data) =>
        g.console.log(s"chartData = ${JSON.stringify(data)}")
        chartData = data
      case Failure(e) =>
        g.console.error(s"Failed to load ${JSON.stringify(exposure)} data")
    }
  }

  $scope.colorFunction = () => (d: js.Dynamic, i: Double) => colors(i.toInt % colors.length)

  $scope.xFunction = { () => (d: js.Dynamic) => d.label }: js.Function

  $scope.yFunction = { () => (d: js.Dynamic) => d.value }: js.Function

  /**
   * Initializes the view by displaying an initial chart
   */
  private def init() {
    if (mySession.userProfile.OID_?.isDefined && mySession.contest.isDefined) {
      for {
        userID <- mySession.userProfile.OID_?
        contest <- mySession.contest
      } {
        $scope.exposurePieChart(contest, $scope.selectedExposure.value, userID)
      }
    }
    else {
      $timeout(() => init(), 1000)
    }
  }

}
