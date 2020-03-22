package com.shocktrade.client.contest

import com.shocktrade.client.RootScope
import com.shocktrade.client.ScopeEvents._
import com.shocktrade.client.contest.DashboardController.DashboardRouteParams
import com.shocktrade.client.contest.ExposureController.ExposureSelection
import com.shocktrade.client.users.GameStateFactory
import com.shocktrade.common.models.ExposureData
import io.scalajs.dom.html.browser.console
import io.scalajs.npm.amcharts.AmChart.Export
import io.scalajs.npm.amcharts.{AmCharts, AmPieChart}
import io.scalajs.npm.angularjs.AngularJsHelper._
import io.scalajs.npm.angularjs.nvd3._
import io.scalajs.npm.angularjs.nvd3.chart._
import io.scalajs.npm.angularjs.toaster.Toaster
import io.scalajs.npm.angularjs.{Controller, angular, injected}
import io.scalajs.util.PromiseHelper.Implicits._

import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js
import scala.scalajs.js.JSConverters._
import scala.util.{Failure, Success}

/**
 * Exposure Controller
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
class ExposureController($scope: ExposureControllerScope, $routeParams: DashboardRouteParams, toaster: Toaster,
                         @injected("GameStateFactory") gameState: GameStateFactory,
                         @injected("PortfolioService") portfolioService: PortfolioService)
  extends Controller {

  implicit private val scope: ExposureControllerScope = $scope

  ///////////////////////////////////////////////////////////////////////////
  //          Public Variables
  ///////////////////////////////////////////////////////////////////////////

  $scope.data = null
  $scope.exposures = js.Array(
    new ExposureSelection(value = "sector", label = "Sector Exposure"),
    new ExposureSelection(value = "industry", label = "Industry Exposure"),
    new ExposureSelection(value = "exchange", label = "Exchange Exposure"),
    new ExposureSelection(value = "securities", label = "Securities Exposure"))

  $scope.selectedExposure = $scope.exposures.headOption.orUndefined
  $scope.options = new ChartOptions(
    new PieChart(
      width = 800,
      height = 400,
      donut = true,
      donutRatio = 0.25,
      x = (d: ExposureData) => d.name,
      y = (d: ExposureData) => d.value,
      labelThreshold = 0.01,
      showLabels = false,
      showLegend = true
    ))

  ///////////////////////////////////////////////////////////////////////////
  //          Initialization Functions
  ///////////////////////////////////////////////////////////////////////////

  def init(): Unit = {
    console.info(s"Initializing ${getClass.getSimpleName}...")
    for {
      contestID <- $routeParams.contestID
      userID <- gameState.userID
    } $scope.exposureChart(contestID, userID, $scope.exposures.headOption.orUndefined)
  }

  $scope.onUserProfileUpdated { (_, _) => init() }

  ///////////////////////////////////////////////////////////////////////////
  //          Public Functions
  ///////////////////////////////////////////////////////////////////////////

  $scope.exposureChart = (aContestID: js.UndefOr[String], aUserID: js.UndefOr[String], anExposure: js.UndefOr[ExposureSelection]) => {
    for {
      contestID <- aContestID
      userID <- aUserID
      exposure <- anExposure
      value <- exposure.value
    } {
      portfolioService.getChartData(contestID, userID, value) onComplete {
        case Success(response) =>
          updateChart(response.data)
          $scope.$apply(() => $scope.data = response.data)
        case Failure(e) =>
          toaster.error(s"Error loading ${exposure.label.orNull}")
          console.error(s"Failed to load exposure data for ${exposure.label.orNull}: ${e.displayMessage}")
      }
    }
  }

  def updateChart(datums: js.Array[ExposureData]): Unit = {
    AmCharts.makeChart(container = "chart_div", AmPieChart(
      dataProvider = datums,
      titleField = "name",
      valueField = "value",
      labelRadius = 30.0,
      angle = 30.0,
      outlineAlpha = 0.4,
      depth3D = 15.0,
     // balloonText = """[[title]]<br><span style='font-size:14px'><b>[[value]]</b> ([[percents]]%)</span>""",
     // startDuration = js.undefined,
      export = new Export(enabled = false),
      theme = "light",
      listeners = js.Array(AmPieChart.onClickSlice { clickEvent =>
        console.log(s"Click event occurred - ${angular.toJson(clickEvent)}")
      })
    ))
    ()
  }

  ///////////////////////////////////////////////////////////////////////////
  //          Events
  ///////////////////////////////////////////////////////////////////////////

  $scope.onUserProfileUpdated { (_, profile) => init() }

}

/**
 * Exposure Controller Companion
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
object ExposureController {

  /**
   * Exposure Selection Model
   * @author Lawrence Daniels <lawrence.daniels@gmail.com>
   */
  class ExposureSelection(val label: js.UndefOr[String], val value: js.UndefOr[String]) extends js.Object

}

/**
 * Exposure Controller Scope
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
@js.native
trait ExposureControllerScope extends RootScope {
  // variables
  var data: js.Array[_ <: js.Any] = js.native
  var exposures: js.Array[ExposureSelection] = js.native
  var options: ChartOptions = js.native
  var selectedExposure: js.UndefOr[ExposureSelection] = js.native

  // functions
  var exposureChart: js.Function3[js.UndefOr[String], js.UndefOr[String], js.UndefOr[ExposureSelection], Unit] = js.native

}