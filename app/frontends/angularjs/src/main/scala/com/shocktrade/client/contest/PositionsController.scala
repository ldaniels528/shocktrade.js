package com.shocktrade.client.contest

import com.shocktrade.client.ScopeEvents._
import com.shocktrade.client.contest.DashboardController.DashboardRouteParams
import com.shocktrade.client.contest.PositionsController.PositionsControllerScope
import com.shocktrade.client.dialogs.NewOrderDialog
import com.shocktrade.client.dialogs.NewOrderDialogController.{NewOrderDialogResult, NewOrderParams}
import com.shocktrade.client.models.contest.Position
import com.shocktrade.client.users.GameStateFactory
import com.shocktrade.client.{GlobalLoading, RootScope}
import io.scalajs.dom.html.browser.console
import io.scalajs.npm.angularjs.AngularJsHelper._
import io.scalajs.npm.angularjs.toaster.Toaster
import io.scalajs.npm.angularjs.{Controller, Timeout, injected}
import io.scalajs.util.PromiseHelper.Implicits._

import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js
import scala.util.{Failure, Success}

/**
 * Positions Controller
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
class PositionsController($scope: PositionsControllerScope, $routeParams: DashboardRouteParams, $timeout: Timeout, toaster: Toaster,
                          @injected("GameStateFactory") gameState: GameStateFactory,
                          @injected("NewOrderDialog") newOrderDialog: NewOrderDialog,
                          @injected("PortfolioService") portfolioService: PortfolioService)
  extends Controller with GlobalLoading {

  implicit private val scope: PositionsControllerScope = $scope

  $scope.selectedPosition = js.undefined

  /////////////////////////////////////////////////////////////////////
  //          Initialization Functions
  /////////////////////////////////////////////////////////////////////

  $scope.initPositions = () => for (contestID <- $routeParams.contestID; userID <- gameState.userID) {
    loadPositions(contestID, userID)
  }

  $scope.onUserProfileUpdated { (_, _) => $scope.initPositions() }

  private def loadPositions(contestID: String, userID: String): Unit = {
    portfolioService.findPositions(contestID, userID) onComplete {
      case Success(orders) => $scope.$apply(() => $scope.positions = orders.data)
      case Failure(e) =>
        toaster.error("Failed to retrieve orders")
        console.error(s"Failed to retrieve orders: ${e.displayMessage}")
    }
  }

  /////////////////////////////////////////////////////////////////////
  //          Position Functions
  /////////////////////////////////////////////////////////////////////

  $scope.getPositions = () => $scope.positions

  $scope.isPositionSelected = () => $scope.getPositions().nonEmpty && $scope.selectedPosition.nonEmpty

  $scope.selectPosition = (position: js.UndefOr[Position]) => $scope.selectedPosition = position

  $scope.sellPosition = (aSymbol: js.UndefOr[String], aQuantity: js.UndefOr[Double]) => {
    for {
      contestID <- $routeParams.contestID
      userID <- gameState.userID
      symbol <- aSymbol
      quantity <- aQuantity
    } yield newOrderDialog.popup(new NewOrderParams(contestID, userID, symbol = symbol, quantity = quantity))
  }

  $scope.toggleSelectedPosition = () => $scope.selectedPosition = js.undefined

  $scope.tradingStart = () => new js.Date()

}

/**
 * Positions Controller Companion
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
object PositionsController {

  /**
   * Positions Controller Scope
   * @author Lawrence Daniels <lawrence.daniels@gmail.com>
   */
  @js.native
  trait PositionsControllerScope extends RootScope {
    // functions
    var initPositions: js.Function0[Unit] = js.native
    var getPositions: js.Function0[js.UndefOr[js.Array[Position]]] = js.native
    var isPositionSelected: js.Function0[Boolean] = js.native
    var selectPosition: js.Function1[js.UndefOr[Position], Unit] = js.native
    var sellPosition: js.Function2[js.UndefOr[String], js.UndefOr[Double], js.UndefOr[js.Promise[NewOrderDialogResult]]] = js.native
    var toggleSelectedPosition: js.Function0[Unit] = js.native
    var tradingStart: js.Function0[js.Date] = js.native

    // variables
    var positions: js.UndefOr[js.Array[Position]] = js.native
    var selectedPosition: js.UndefOr[Position] = js.native
  }

}