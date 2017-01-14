package com.shocktrade.client.contest

import scala.scalajs.js.JSConverters._
import com.shocktrade.client.contest.ExposureController.ExposureSelection
import com.shocktrade.common.models.ExposureData
import org.scalajs.angularjs.AngularJsHelper._
import org.scalajs.angularjs.nvd3._
import org.scalajs.angularjs.nvd3.chart._
import org.scalajs.angularjs.toaster.Toaster
import org.scalajs.angularjs.{Controller, Scope, angular, injected}
import org.scalajs.dom.browser.console

import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js
import scala.scalajs.js.annotation.ScalaJSDefined
import scala.util.{Failure, Success}

/**
  * Exposure Controller
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
class ExposureController($scope: ExposureControllerScope, toaster: Toaster,
                         @injected("PortfolioService") portfolioService: PortfolioService) extends Controller {

  ///////////////////////////////////////////////////////////////////////////
  //          Public Variables
  ///////////////////////////////////////////////////////////////////////////

  $scope.data = null

  $scope.exposures = js.Array(
    new ExposureSelection(value = "sector", label = "Sector Exposure"),
    new ExposureSelection(value = "industry", label = "Industry Exposure"),
    new ExposureSelection(value = "exchange", label = "Exchange Exposure"),
    new ExposureSelection(value = "market", label = "Exchange Sub-Market Exposure"),
    new ExposureSelection(value = "securities", label = "Securities Exposure"))

  $scope.selectedExposure = $scope.exposures.headOption.orUndefined

  $scope.options = new ChartOptions(
    new PieChart(
      width = 800,
      height = 400,
      donut = true,
      donutRatio = 0.25,
      x = (d: ExposureData) => d.key,
      y = (d: ExposureData) => d.value,
      labelThreshold = 0.01,
      showLabels = false,
      showLegend = true
    ))

  console.log(s"options = ${angular.toJson($scope.options, pretty = true)}")

  ///////////////////////////////////////////////////////////////////////////
  //          Public Functions
  ///////////////////////////////////////////////////////////////////////////

  $scope.exposureChart = (aContestID: js.UndefOr[String], aUserID: js.UndefOr[String], anExposure: js.UndefOr[ExposureSelection]) => {
    for {
      contestID <- aContestID
      userID <- aUserID
      exposure <- anExposure
    } {
      portfolioService.getExposureChartData(contestID, userID, exposure.value) onComplete {
        case Success(data) => $scope.$apply(() => $scope.data = data)
        case Failure(e) =>
          toaster.error(s"Error loading ${exposure.label}")
          console.error(s"Failed to load exposure data for ${exposure.label}: ${e.displayMessage}")
      }
    }
  }

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
  @ScalaJSDefined
  class ExposureSelection(val label: String, val value: String) extends js.Object

}

/**
  * Exposure Controller Scope
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
@js.native
trait ExposureControllerScope extends Scope {
  // variables
  var data: js.Array[_ <: js.Any] = js.native
  var exposures: js.Array[ExposureSelection] = js.native
  var options: ChartOptions = js.native
  var selectedExposure: js.UndefOr[ExposureSelection] = js.native

  // functions
  var exposureChart: js.Function3[js.UndefOr[String], js.UndefOr[String], js.UndefOr[ExposureSelection], Unit] = js.native

}