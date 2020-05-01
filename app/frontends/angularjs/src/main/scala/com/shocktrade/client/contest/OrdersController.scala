package com.shocktrade.client.contest

import com.shocktrade.client.GameState._
import com.shocktrade.client.GlobalLoading
import com.shocktrade.client.ScopeEvents._
import com.shocktrade.client.contest.DashboardController._
import com.shocktrade.client.contest.OrdersController.OrdersControllerScope
import com.shocktrade.client.users.UserService
import com.shocktrade.common.models.contest.{Order, Portfolio}
import com.shocktrade.common.models.user.UserProfile
import io.scalajs.dom.html.browser.console
import io.scalajs.npm.angularjs.AngularJsHelper._
import io.scalajs.npm.angularjs.cookies.Cookies
import io.scalajs.npm.angularjs.http.HttpResponse
import io.scalajs.npm.angularjs.toaster.Toaster
import io.scalajs.npm.angularjs.{Controller, Scope, Timeout, injected}
import io.scalajs.util.DurationHelper._
import io.scalajs.util.JsUnderOrHelper._
import io.scalajs.util.PromiseHelper.Implicits._

import scala.concurrent.duration._
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js
import scala.scalajs.js.JSConverters._
import scala.util.{Failure, Success}

/**
 * Orders Controller
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
class OrdersController($scope: OrdersControllerScope, $routeParams: DashboardRouteParams,
                       $cookies: Cookies, $timeout: Timeout, toaster: Toaster,
                       @injected("PortfolioService") portfolioService: PortfolioService,
                       @injected("UserService") userService: UserService)
  extends Controller with GlobalLoading {

  implicit val cookies: Cookies = $cookies
  private val marketOrderTypes = js.Array("MARKET", "MARKET_ON_CLOSE")

  $scope.showOpenOrders = true
  $scope.showClosedOrders = true
  $scope.selectedOrder = js.undefined
  $scope.orders = js.undefined

  /////////////////////////////////////////////////////////////////////
  //          Initialization Functions
  /////////////////////////////////////////////////////////////////////

  $scope.initOrders = () => {
    for {
      contestID <- $routeParams.contestID
      userID <- $cookies.getGameState.userID
    } yield initOrders(contestID, userID)
  }

  $scope.onUserProfileUpdated { (_, _) => $scope.initOrders() }

  private def initOrders(contestID: String, userID: String): js.Promise[(HttpResponse[UserProfile], HttpResponse[js.Array[Order]])] = {
    val outcome = for {
      userProfile <- userService.findUserByID(userID)
      orders <- portfolioService.findOrders(contestID, userID)
    } yield (userProfile, orders)

    outcome onComplete {
      case Success((userProfile, orders)) =>
        $scope.$apply { () =>
          $scope.userProfile = userProfile.data
          $scope.orders = orders.data
        }
      case Failure(e) => console.error(s"Failed to retrieve user profile: ${e.getMessage}")
    }
    outcome.toJSPromise
  }

  /////////////////////////////////////////////////////////////////////
  //          Active Order Functions
  /////////////////////////////////////////////////////////////////////

  $scope.cancelOrder = (anOrderID: js.UndefOr[String]) => anOrderID map cancelOrder

  $scope.computeOrderCost = (anOrder: js.UndefOr[Order]) => anOrder.flatMap(_.totalCost)

  $scope.getOrders = () => ($scope.showOpenOrders.isTrue, $scope.showClosedOrders.isTrue) match {
    case (true, true) => $scope.orders
    case (true, false) => $scope.orders.map(_.filter(_.closed.contains(0)))
    case (false, true) => $scope.orders.map(_.filter(_.closed.contains(1)))
    case (false, false) => js.Array[Order]()
  }

  $scope.isMarketOrder = (anOrder: js.UndefOr[Order]) => {
    anOrder.exists(order => order.priceType.exists(marketOrderTypes.contains))
  }

  $scope.isOrderSelected = () => $scope.getOrders().nonEmpty && $scope.selectedOrder.nonEmpty

  $scope.selectOrder = (order: js.UndefOr[Order]) => $scope.selectedOrder = order

  $scope.toggleSelectedOrder = () => $scope.selectedOrder = js.undefined

  private def cancelOrder(orderID: String): js.Promise[HttpResponse[Portfolio]] = {
    $scope.isCancelingOrder = true
    val outcome = portfolioService.cancelOrder(orderID)
    outcome onComplete {
      case Success(_) =>
      case Failure(err) =>
        toaster.error("Failed to cancel order")
        console.error(s"Failed to cancel order: ${err.displayMessage}")
    }
    outcome onComplete { _ =>
      $timeout(() => $scope.isCancelingOrder = false, 500.millis)
    }
    outcome
  }

}

/**
 * Orders Controller Companion
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
object OrdersController {

  /**
   * Orders Controller Scope
   * @author Lawrence Daniels <lawrence.daniels@gmail.com>
   */
  @js.native
  trait OrdersControllerScope extends Scope {
    // functions
    var initOrders: js.Function0[js.UndefOr[js.Promise[(HttpResponse[UserProfile], HttpResponse[js.Array[Order]])]]] = js.native
    var computeOrderCost: js.Function1[js.UndefOr[Order], js.UndefOr[Double]] = js.native
    var cancelOrder: js.Function1[js.UndefOr[String], js.UndefOr[js.Promise[HttpResponse[Portfolio]]]] = js.native
    var getOrders: js.Function0[js.UndefOr[js.Array[Order]]] = js.native
    var isMarketOrder: js.Function1[js.UndefOr[Order], Boolean] = js.native
    var isOrderSelected: js.Function0[Boolean] = js.native
    var selectOrder: js.Function1[js.UndefOr[Order], Unit] = js.native
    var toggleSelectedOrder: js.Function0[Unit] = js.native

    // variables
    var orders: js.UndefOr[js.Array[Order]] = js.native
    var isCancelingOrder: js.UndefOr[Boolean] = js.native
    var selectedOrder: js.UndefOr[Order] = js.native
    var showClosedOrders: js.UndefOr[Boolean] = js.native
    var showOpenOrders: js.UndefOr[Boolean] = js.native
    var userProfile: js.UndefOr[UserProfile] = js.native
  }

}
