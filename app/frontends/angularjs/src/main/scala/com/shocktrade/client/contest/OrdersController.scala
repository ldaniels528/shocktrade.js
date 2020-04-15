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
import io.scalajs.util.JsUnderOrHelper._
import io.scalajs.util.PromiseHelper.Implicits._

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
  $scope.showClosedOrders = false
  $scope.selectedOrder = js.undefined
  $scope.activeOrders = js.undefined

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
          $scope.activeOrders = orders.data
        }
      case Failure(e) => console.error(s"Failed to retrieve user profile: ${e.getMessage}")
    }
    outcome.toJSPromise
  }

  /////////////////////////////////////////////////////////////////////
  //          Active Order Functions
  /////////////////////////////////////////////////////////////////////

  $scope.cancelOrder = (aPortfolioId: js.UndefOr[String], anOrderId: js.UndefOr[String]) => {
    for {
      portfolioID <- aPortfolioId
      orderID <- anOrderId
    } yield cancelOrder(portfolioID, orderID)
  }

  $scope.computeOrderCost = (anOrder: js.UndefOr[Order]) => anOrder.flatMap(_.totalCost)

  $scope.getActiveOrders = () => ($scope.showOpenOrders.isTrue, $scope.showClosedOrders.isTrue) match {
    case (true, true) => $scope.activeOrders
    case (true, false) => $scope.activeOrders.map(_.filter(_.closed.isTrue))
    case (false, true) => $scope.activeOrders.map(_.filterNot(_.closed.isTrue))
    case (false, false) => js.Array[Order]()
  }

  $scope.isMarketOrder = (anOrder: js.UndefOr[Order]) => {
    anOrder.exists(order => order.priceType.exists(marketOrderTypes.contains))
  }

  $scope.isOrderSelected = () => $scope.getActiveOrders().nonEmpty && $scope.selectedOrder.nonEmpty

  $scope.selectOrder = (order: js.UndefOr[Order]) => $scope.selectedOrder = order

  $scope.toggleSelectedOrder = () => $scope.selectedOrder = js.undefined

  private def cancelOrder(portfolioID: String, orderID: String): js.Promise[HttpResponse[Portfolio]] = {
    val outcome = portfolioService.cancelOrder(portfolioID, orderID)
    asyncLoading($scope)(outcome) onComplete {
      case Success(portfolio) => $scope.$apply { () => }
      case Failure(err) =>
        toaster.error("Failed to cancel order")
        console.error(s"Failed to cancel order: ${err.displayMessage}")
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
    var cancelOrder: js.Function2[js.UndefOr[String], js.UndefOr[String], js.UndefOr[js.Promise[HttpResponse[Portfolio]]]] = js.native
    var getActiveOrders: js.Function0[js.UndefOr[js.Array[Order]]] = js.native
    var isMarketOrder: js.Function1[js.UndefOr[Order], Boolean] = js.native
    var isOrderSelected: js.Function0[Boolean] = js.native
    var selectOrder: js.Function1[js.UndefOr[Order], Unit] = js.native
    var toggleSelectedOrder: js.Function0[Unit] = js.native

    // variables
    var activeOrders: js.UndefOr[js.Array[Order]] = js.native
    var selectedOrder: js.UndefOr[Order] = js.native
    var showClosedOrders: js.UndefOr[Boolean] = js.native
    var showOpenOrders: js.UndefOr[Boolean] = js.native
    var userProfile: js.UndefOr[UserProfile] = js.native
  }

}
