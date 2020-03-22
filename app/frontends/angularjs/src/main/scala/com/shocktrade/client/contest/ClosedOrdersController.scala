package com.shocktrade.client.contest

import com.shocktrade.client.ScopeEvents._
import com.shocktrade.client.contest.ClosedOrdersController.ClosedOrdersControllerScope
import com.shocktrade.client.contest.DashboardController._
import com.shocktrade.client.models.contest.Order
import com.shocktrade.client.users.GameStateFactory
import com.shocktrade.client.{GlobalLoading, RootScope}
import io.scalajs.dom.html.browser.console
import io.scalajs.npm.angularjs.AngularJsHelper._
import io.scalajs.npm.angularjs.toaster.Toaster
import io.scalajs.npm.angularjs.{Controller, Timeout, injected}
import io.scalajs.util.JsUnderOrHelper._
import io.scalajs.util.PromiseHelper.Implicits._

import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js
import scala.util.{Failure, Success}

/**
 * Closed Orders Controller
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
class ClosedOrdersController($scope: ClosedOrdersControllerScope, $routeParams: DashboardRouteParams, $timeout: Timeout, toaster: Toaster,
                             @injected("GameStateFactory") gameState: GameStateFactory,
                             @injected("PortfolioService") portfolioService: PortfolioService)
  extends Controller with GlobalLoading {

  implicit private val scope: ClosedOrdersControllerScope = $scope

  $scope.closedOrders = js.undefined
  $scope.selectedClosedOrder = js.undefined

  /////////////////////////////////////////////////////////////////////
  //          Initialization Functions
  /////////////////////////////////////////////////////////////////////

  $scope.initClosedOrders = () => {
    for (contestID <- $routeParams.contestID; userID <- gameState.userID) {
      loadClosedOrders(contestID, userID)
    }
  }

  $scope.onUserProfileUpdated { (_, _) => $scope.initClosedOrders() }

  private def loadClosedOrders(contestID: String, userID: String): Unit = {
    portfolioService.findOrders(contestID, userID) onComplete {
      case Success(orders) => $scope.$apply(() => $scope.closedOrders = orders.data.filter(_.closed.isTrue))
      case Failure(e) =>
        toaster.error("Failed to retrieve orders")
        console.error(s"Failed to retrieve orders: ${e.displayMessage}")
    }
  }

  /////////////////////////////////////////////////////////////////////
  //          Closed Order Functions
  /////////////////////////////////////////////////////////////////////

  $scope.getClosedOrders = () => $scope.closedOrders

  $scope.isClosedOrderSelected = () => $scope.getClosedOrders().nonEmpty && $scope.selectedClosedOrder.nonEmpty

  $scope.selectClosedOrder = (closeOrder: js.UndefOr[Order]) => $scope.selectedClosedOrder = closeOrder

  $scope.toggleSelectedClosedOrder = () => $scope.selectedClosedOrder = js.undefined

}

/**
 * Closed Orders Controller
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
object ClosedOrdersController {

  @js.native
  trait ClosedOrdersControllerScope extends RootScope{
    // functions
    var initClosedOrders: js.Function0[Unit] = js.native
    var getClosedOrders: js.Function0[js.UndefOr[js.Array[Order]]] = js.native
    var isClosedOrderSelected: js.Function0[Boolean] = js.native
    var selectClosedOrder: js.Function1[js.UndefOr[Order], Unit] = js.native
    var toggleSelectedClosedOrder: js.Function0[Unit] = js.native

    // variables
    var selectedClosedOrder: js.UndefOr[Order] = js.native
    var closedOrders: js.UndefOr[js.Array[Order]] = js.native

  }

}