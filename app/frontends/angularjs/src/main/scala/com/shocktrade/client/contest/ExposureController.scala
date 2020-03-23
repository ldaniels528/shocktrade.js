package com.shocktrade.client.contest

import com.shocktrade.client.RootScope
import com.shocktrade.client.ScopeEvents._
import com.shocktrade.client.contest.DashboardController.DashboardRouteParams
import com.shocktrade.client.contest.ExposureController._
import com.shocktrade.client.models.UserProfile
import com.shocktrade.client.users.GameStateFactory
import com.shocktrade.common.models.ExposureData
import io.scalajs.dom.Event
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

  $scope.exposures = js.Array(
    new ExposureSelection(value = "securities", label = "Securities Exposure"),
    new ExposureSelection(value = "exchange", label = "Exchange Exposure"),
    new ExposureSelection(value = "industry", label = "Industry Exposure"),
    new ExposureSelection(value = "sector", label = "Sector Exposure"))

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

  $scope.initChart = () => initChart()

  def initChart(): Unit = {
    console.info(s"Initializing ${getClass.getSimpleName}...")
    $scope.exposureChart($scope.selectedExposure)
  }

  $scope.onUserProfileUpdated { (_: Event, _: UserProfile) => initChart() }

  ///////////////////////////////////////////////////////////////////////////
  //          Public Functions
  ///////////////////////////////////////////////////////////////////////////

  $scope.exposureChart = (anExposure: js.UndefOr[ExposureSelection]) => {
    for {
      contestID <- $routeParams.contestID
      userID <- gameState.userID
      exposure <- anExposure
      value <- exposure.value
    } {
      portfolioService.getChartData(contestID, userID, value) onComplete {
        case Success(response) => updateChartDiv(response.data)
        case Failure(e) =>
          toaster.error(s"Error loading ${exposure.label.orNull}")
          console.error(s"Failed to load exposure data for ${exposure.label.orNull}: ${e.displayMessage}")
      }
    }
  }

  private def updateChartDiv(datums: js.Array[ExposureData]): Unit = {
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

}

/**
 * Exposure Controller Companion
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
object ExposureController {

  /**
   * Exposure Controller Scope
   * @author Lawrence Daniels <lawrence.daniels@gmail.com>
   */
  @js.native
  trait ExposureControllerScope extends RootScope {
    // variables
    var exposures: js.Array[ExposureSelection] = js.native
    var options: ChartOptions = js.native
    var selectedExposure: js.UndefOr[ExposureSelection] = js.native

    // functions
    var initChart: js.Function0[Unit] = js.native
    var exposureChart: js.Function1[js.UndefOr[ExposureSelection], Unit] = js.native

  }

  /**
   * Exposure Selection Model
   * @author Lawrence Daniels <lawrence.daniels@gmail.com>
   */
  class ExposureSelection(val label: js.UndefOr[String], val value: js.UndefOr[String]) extends js.Object

}