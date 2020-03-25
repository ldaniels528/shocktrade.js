package com.shocktrade.client.contest

import com.shocktrade.client.contest.DashboardController.DashboardRouteParams
import com.shocktrade.client.contest.PerformanceController._
import com.shocktrade.client.models.contest.Performance
import com.shocktrade.client.users.GameStateFactory
import com.shocktrade.client.{GlobalLoading, RootScope}
import io.scalajs.dom.html.browser.console
import io.scalajs.npm.angularjs.toaster.Toaster
import io.scalajs.npm.angularjs.{Controller, Timeout, injected}

import scala.scalajs.js

/**
 * Performance Controller
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
class PerformanceController($scope: PerformanceControllerScope, $routeParams: DashboardRouteParams, $timeout: Timeout, toaster: Toaster,
                            @injected("GameStateFactory") gameState: GameStateFactory,
                            @injected("PortfolioService") portfolioService: PortfolioService)
  extends Controller with GlobalLoading {

  private var performances: js.Array[Performance] = js.Array()

  $scope.initPerformance = () => {
    console.info(s"Initializing ${PerformanceController.getClass.getSimpleName}...")
  }

  /////////////////////////////////////////////////////////////////////
  //          Performance Functions
  /////////////////////////////////////////////////////////////////////

  $scope.getPerformance = () => performances

  $scope.isPerformanceSelected = () => performances.nonEmpty && $scope.selectedPerformance.nonEmpty

  $scope.selectPerformance = (performance: js.UndefOr[Performance]) => $scope.selectedPerformance = performance

  $scope.toggleSelectedPerformance = () => $scope.selectedPerformance = js.undefined

  $scope.cost = (aTx: js.UndefOr[Performance]) => aTx.flatMap(_.totalCost)

  $scope.soldValue = (aTx: js.UndefOr[Performance]) => aTx.flatMap(_.totalSold)

  $scope.proceeds = (aTx: js.UndefOr[Performance]) => aTx.flatMap(_.proceeds)

  $scope.gainLoss = (aTx: js.UndefOr[Performance]) => aTx.flatMap(_.gainLoss)

}

/**
 * Performance Controller Companion
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
object PerformanceController {

  /**
   * Performance Controller Scope
   * @author Lawrence Daniels <lawrence.daniels@gmail.com>
   */
  @js.native
  trait PerformanceControllerScope extends RootScope {
    // functions
    var initPerformance: js.Function0[Unit] = js.native
    var cost: js.Function1[js.UndefOr[Performance], js.UndefOr[Double]] = js.native
    var gainLoss: js.Function1[js.UndefOr[Performance], js.UndefOr[Double]] = js.native
    var getPerformance: js.Function0[js.UndefOr[js.Array[Performance]]] = js.native
    var isPerformanceSelected: js.Function0[Boolean] = js.native
    var proceeds: js.Function1[js.UndefOr[Performance], js.UndefOr[Double]] = js.native
    var selectPerformance: js.Function1[js.UndefOr[Performance], Unit] = js.native
    var soldValue: js.Function1[js.UndefOr[Performance], js.UndefOr[Double]] = js.native
    var toggleSelectedPerformance: js.Function0[Unit] = js.native

    // variables
    var selectedPerformance: js.UndefOr[Performance] = js.native
  }

}