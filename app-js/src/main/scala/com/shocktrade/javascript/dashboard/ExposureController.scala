package com.shocktrade.javascript.dashboard

import com.github.ldaniels528.scalascript.core.{Http, Timeout}
import com.github.ldaniels528.scalascript.extensions.Toaster
import com.github.ldaniels528.scalascript.util.ScalaJsHelper._
import com.github.ldaniels528.scalascript.{Controller, injected}
import com.shocktrade.javascript.MySession
import com.shocktrade.javascript.models.BSONObjectID
import org.scalajs.dom.console

import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js
import scala.scalajs.js.Dynamic.{literal => JS}
import scala.util.{Failure, Success}

/**
  * Exposure Controller
  * @author lawrence.daniels@gmail.com
  */
class ExposureController($scope: js.Dynamic, $http: Http, $timeout: Timeout, toaster: Toaster,
                         @injected("ContestService") contestService: ContestService,
                         @injected("MySession") mySession: MySession)
  extends Controller {

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
    for {
      cid <- contestID
      uid <- userID
      eid <- exposure
    } exposurePieChart(BSONObjectID(cid), BSONObjectID(uid), eid)
  }

  $scope.colorFunction = () => (d: js.Dynamic, i: Double) => colors(i.toInt % colors.length)

  $scope.xFunction = () => { (d: js.Dynamic) => d.label }: js.Function1[js.Dynamic, js.Dynamic]

  $scope.yFunction = () => { (d: js.Dynamic) => d.value }: js.Function1[js.Dynamic, js.Dynamic]

  ///////////////////////////////////////////////////////////////////////////
  //          Private Functions
  ///////////////////////////////////////////////////////////////////////////

  private def exposurePieChart(contestID: js.UndefOr[BSONObjectID], userID: js.UndefOr[BSONObjectID], exposure: js.UndefOr[String]) = {
    contestService.getExposureChartData(contestID, userID, exposure) onComplete {
      case Success(data) => chartData = data
      case Failure(e) =>
        console.error(s"Failed to load exposure data for $exposure")
    }
  }

}
