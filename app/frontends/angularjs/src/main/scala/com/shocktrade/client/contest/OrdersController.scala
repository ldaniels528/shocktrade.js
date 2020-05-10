package com.shocktrade.client.contest

import com.shocktrade.client.contest.OrdersController.OrdersControllerScope
import com.shocktrade.client.contest.models.Order
import io.scalajs.npm.angularjs.{Controller, Scope}
import io.scalajs.util.JsUnderOrHelper._

import scala.scalajs.js

/**
 * Orders Controller
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
trait OrdersController {
  ref: Controller =>

  private val marketOrderTypes = js.Array("MARKET")

  def $scope: OrdersControllerScope

  $scope.showOpenOrders = true
  $scope.showClosedOrders = false

  /////////////////////////////////////////////////////////////////////
  //          Active Order Functions
  /////////////////////////////////////////////////////////////////////

  $scope.computeOrderCost = (anOrder: js.UndefOr[Order]) => anOrder.flatMap(_.totalCost)

  $scope.getOrders = () => ($scope.showOpenOrders.isTrue, $scope.showClosedOrders.isTrue) match {
    case (true, true) => $scope.orders
    case (true, false) => $scope.orders.map(_.filter(_.closed.contains(0)))
    case (false, true) => $scope.orders.map(_.filter(_.closed.contains(1)))
    case (false, false) => js.Array[Order]()
  }

  $scope.hideShowClosedOrders = () => $scope.showClosedOrders = !$scope.showClosedOrders.isTrue

  $scope.hideShowOpenOrders = () => $scope.showOpenOrders = !$scope.showOpenOrders.isTrue

  $scope.isMarketOrder = (anOrder: js.UndefOr[Order]) => anOrder.exists(_.priceType.exists(marketOrderTypes.contains))

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
    ref: Scope =>

    // functions
    var computeOrderCost: js.Function1[js.UndefOr[Order], js.UndefOr[Double]] = js.native
    var getOrders: js.Function0[js.UndefOr[js.Array[Order]]] = js.native
    var hideShowOpenOrders: js.Function0[Unit] = js.native
    var hideShowClosedOrders: js.Function0[Unit] = js.native
    var isMarketOrder: js.Function1[js.UndefOr[Order], Boolean] = js.native

    // variables
    var orders: js.UndefOr[js.Array[Order]] = js.native
    var isCancelingOrder: js.UndefOr[Boolean] = js.native
    var showClosedOrders: js.UndefOr[Boolean] = js.native
    var showOpenOrders: js.UndefOr[Boolean] = js.native
  }

}