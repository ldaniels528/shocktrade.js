package com.shocktrade.client.contest

import com.shocktrade.client.contest.OrdersController.{OrderSearchFilter, OrdersControllerScope, OrdersInputs}
import com.shocktrade.client.contest.models.Order
import com.shocktrade.common.forms.OrderSearchOptions
import com.shocktrade.common.forms.OrderSearchOptions.OrderStatus
import io.scalajs.dom.html.browser.console
import io.scalajs.npm.angularjs.http.HttpResponse
import io.scalajs.npm.angularjs.{Controller, Scope, Timeout}
import io.scalajs.util.DurationHelper._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.scalajs.js
import scala.scalajs.js.JSConverters._
import scala.util.{Failure, Success}

/**
 * Orders Controller
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
trait OrdersController {
  ref: Controller =>

  private val marketOrderTypes = js.Array("MARKET")

  def $scope: OrdersControllerScope

  def $timeout: Timeout

  def portfolioService: PortfolioService

  $scope.orderFilters = js.Array(
    new OrderSearchFilter(label = "Active", value = OrderSearchOptions.ACTIVE_ORDERS),
    new OrderSearchFilter(label = "Completed", value = OrderSearchOptions.COMPLETED_ORDERS),
    new OrderSearchFilter(label = "Fulfilled", value = OrderSearchOptions.FULFILLED_ORDERS),
    new OrderSearchFilter(label = "Failed", value = OrderSearchOptions.FAILED_ORDERS),
  )
  $scope.orderInputs = new OrdersInputs(status = $scope.orderFilters.headOption.orUndefined)
  $scope.orderSearchOptions = OrderSearchOptions(status = OrderSearchOptions.ACTIVE_ORDERS)

  /////////////////////////////////////////////////////////////////////
  //          Active Order Functions
  /////////////////////////////////////////////////////////////////////

  $scope.computeOrderCost = (anOrder: js.UndefOr[Order]) => anOrder.flatMap(_.totalCost)

  $scope.getOrders = () => $scope.orders

  $scope.loadOrders = (aContestID: js.UndefOr[String], aUserID: js.UndefOr[String]) => {
    for {
      contestID <- aContestID
      userID <- aUserID
      status <- $scope.orderInputs.status.map(_.value)
    } yield loadOrders(contestID, userID, status)
  }

  def loadOrders(contestID: String, userID: String, status: OrderStatus): Future[HttpResponse[js.Array[Order]]] = {
    // update the filter
    $scope.orderSearchOptions.contestID = contestID
    $scope.orderSearchOptions.userID = userID
    $scope.orderSearchOptions.status = status

    // load the orders
    $scope.isLoadingOrders = true
    val outcome = portfolioService.orderSearch($scope.orderSearchOptions).toFuture
    outcome onComplete (_ => $timeout(() => $scope.isLoadingOrders = false, 0.5.seconds))
    outcome onComplete {
      case Success(orders) => $scope.orders = orders.data
      case Failure(e) =>
        console.info(s"getOrders: ${e.getMessage}")
        e.printStackTrace()
    }
    outcome
  }

  $scope.isMarketOrder = (anOrder: js.UndefOr[Order]) => anOrder.exists(_.priceType.exists(marketOrderTypes.contains))

}

/**
 * Orders Controller Companion
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
object OrdersController {

  /**
   * Orders Inputs
   * @param status the given [[OrderStatus]]
   */
  class OrdersInputs(var status: js.UndefOr[OrderSearchFilter]) extends js.Object

  /**
   * Order search filter
   * @param label the given label
   * @param value the given value
   */
  class OrderSearchFilter(val label: String, val value: Int) extends js.Object

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
    var loadOrders: js.Function2[js.UndefOr[String], js.UndefOr[String], js.UndefOr[Future[HttpResponse[js.Array[Order]]]]] = js.native
    var isMarketOrder: js.Function1[js.UndefOr[Order], Boolean] = js.native

    // variables
    var orderInputs: OrdersInputs = js.native
    var orderFilters: js.Array[OrderSearchFilter] = js.native
    var orderSearchOptions: OrderSearchOptions = js.native
    var orders: js.UndefOr[js.Array[Order]] = js.native
    var isCancelingOrder: js.UndefOr[Boolean] = js.native
    var isLoadingOrders: js.UndefOr[Boolean] = js.native
  }

}