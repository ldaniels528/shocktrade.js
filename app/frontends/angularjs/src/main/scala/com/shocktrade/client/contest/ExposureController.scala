package com.shocktrade.client.contest

import com.shocktrade.client.GameState._
import com.shocktrade.client.ScopeEvents._
import com.shocktrade.client.contest.DashboardController.DashboardRouteParams
import com.shocktrade.client.contest.ExposureController._
import com.shocktrade.client.users.UserService
import com.shocktrade.common.models.contest.ChartData
import com.shocktrade.common.models.user.UserProfile
import io.scalajs.dom.Event
import io.scalajs.dom.html.browser.console
import io.scalajs.npm.amcharts.AmChart.Export
import io.scalajs.npm.amcharts.{AmCharts, AmPieChart}
import io.scalajs.npm.angularjs.AngularJsHelper._
import io.scalajs.npm.angularjs.cookies.Cookies
import io.scalajs.npm.angularjs.http.HttpResponse
import io.scalajs.npm.angularjs.nvd3._
import io.scalajs.npm.angularjs.nvd3.chart._
import io.scalajs.npm.angularjs.toaster.Toaster
import io.scalajs.npm.angularjs.{Controller, Scope, angular, injected}
import io.scalajs.util.PromiseHelper.Implicits._

import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js
import scala.scalajs.js.JSConverters._
import scala.util.{Failure, Success}

/**
 * Exposure Controller
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
class ExposureController($scope: ExposureControllerScope, $routeParams: DashboardRouteParams,
                         $cookies: Cookies, toaster: Toaster,
                         @injected("PortfolioService") portfolioService: PortfolioService,
                         @injected("UserService") userService: UserService)
  extends Controller {

  implicit val cookies: Cookies = $cookies

  ///////////////////////////////////////////////////////////////////////////
  //          Public Variables
  ///////////////////////////////////////////////////////////////////////////

  $scope.exposures = js.Array(
    new ChartSelection(value = "contest", label = "Distribution of Wealth"),
    new ChartSelection(value = "securities", label = "Securities Exposure"),
    new ChartSelection(value = "exchange", label = "Exchange Exposure"),
    new ChartSelection(value = "industry", label = "Industry Exposure"),
    new ChartSelection(value = "sector", label = "Sector Exposure"))

  $scope.selectedExposure = $scope.exposures.headOption.orUndefined

  $scope.options = new ChartOptions(
    new PieChart(
      width = 800,
      height = 400,
      donut = true,
      donutRatio = 0.25,
      x = (d: ChartData) => d.name,
      y = (d: ChartData) => d.value,
      labelThreshold = 0.01,
      showLabels = false,
      showLegend = true
    ))

  ///////////////////////////////////////////////////////////////////////////
  //          Initialization Functions
  ///////////////////////////////////////////////////////////////////////////

  $scope.initChart = () => initChart()

  def initChart(): js.UndefOr[js.Promise[HttpResponse[UserProfile]]] = {
    console.info(s"Initializing ${getClass.getSimpleName}...")
    $scope.showChart($scope.selectedExposure)

    // attempt to load the user profile
    $cookies.getGameState.userID map { userID =>
      val outcome = userService.findUserByID(userID)
      outcome onComplete {
        case Success(userProfile) => $scope.$apply(() => $scope.userProfile = userProfile.data)
        case Failure(e) => console.error(s"Failed to retrieve user profile: ${e.getMessage}")
      }
      outcome
    }
  }

  $scope.onUserProfileUpdated { (_: Event, _: UserProfile) => initChart() }

  ///////////////////////////////////////////////////////////////////////////
  //          Public Functions
  ///////////////////////////////////////////////////////////////////////////

  $scope.showChart = (aChart: js.UndefOr[ChartSelection]) => {
    for {
      contestID <- $routeParams.contestID
      userID <- $cookies.getGameState.userID
      chart <- aChart
      value <- chart.value
    } yield showChart(contestID, userID, chart, value)
  }

  private def showChart(contestID: String, userID: String, chart: ChartSelection, value: String): js.Promise[HttpResponse[js.Array[ChartData]]] = {
    val outcome = portfolioService.findChart(contestID, userID, value)
    outcome onComplete {
      case Success(response) => updateChartDiv(response.data)
      case Failure(e) =>
        toaster.error(s"Error loading ${chart.label.orNull}")
        console.error(s"Failed to load exposure data for ${chart.label.orNull}: ${e.displayMessage}")
    }
    outcome
  }

  private def updateChartDiv(datums: js.Array[ChartData]): AmPieChart = {
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
  trait ExposureControllerScope extends Scope {
    // variables
    var exposures: js.Array[ChartSelection] = js.native
    var options: ChartOptions = js.native
    var selectedExposure: js.UndefOr[ChartSelection] = js.native

    // functions
    var initChart: js.Function0[js.UndefOr[js.Promise[HttpResponse[UserProfile]]]] = js.native
    var showChart: js.Function1[js.UndefOr[ChartSelection], js.UndefOr[js.Promise[HttpResponse[js.Array[ChartData]]]]] = js.native
    var userProfile: js.UndefOr[UserProfile] = js.native
  }

  /**
   * Exposure Selection Model
   * @author Lawrence Daniels <lawrence.daniels@gmail.com>
   */
  class ChartSelection(val label: js.UndefOr[String], val value: js.UndefOr[String]) extends js.Object

}