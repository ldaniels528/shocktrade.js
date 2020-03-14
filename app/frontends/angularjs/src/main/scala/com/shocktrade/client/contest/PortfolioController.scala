package com.shocktrade.client.contest

import com.shocktrade.client.ScopeEvents._
import com.shocktrade.client._
import com.shocktrade.client.contest.PortfolioController.PortfolioTab
import com.shocktrade.client.dialogs.NewOrderDialog
import com.shocktrade.client.dialogs.NewOrderDialogController.{NewOrderDialogResult, NewOrderParams}
import com.shocktrade.client.models.contest.{Order, Performance, Portfolio, Position}
import io.scalajs.dom.html.browser.console
import io.scalajs.npm.angularjs.AngularJsHelper._
import io.scalajs.npm.angularjs.cookies.Cookies
import io.scalajs.npm.angularjs.toaster.Toaster
import io.scalajs.npm.angularjs.{Controller, Timeout, injected}
import io.scalajs.util.JsUnderOrHelper._
import io.scalajs.util.PromiseHelper.Implicits._

import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js
import scala.util.{Failure, Success}

/**
 * Portfolio Controller
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
case class PortfolioController($scope: PortfolioScope, $cookies: Cookies, $timeout: Timeout, toaster: Toaster,
                               @injected("ContestService") contestService: ContestService,
                               @injected("NewOrderDialog") newOrderDialog: NewOrderDialog,
                               @injected("QuoteCache") quoteCache: QuoteCache,
                               @injected("PortfolioService") portfolioService: PortfolioService)
  extends Controller with GlobalLoading with GlobalSelectedSymbol {

  private val marketOrderTypes = js.Array("MARKET", "MARKET_ON_CLOSE")

  $scope.closedOrders = js.Array()
  $scope.orders = js.Array()
  $scope.performance = js.Array()
  $scope.portfolios = js.Array()
  $scope.positions = js.Array()

  $scope.selectedClosedOrder = js.undefined
  $scope.selectedOrder = js.undefined
  $scope.selectedPosition = js.undefined

  $scope.portfolioTabs = js.Array(
    new PortfolioTab(name = "Chat", icon = "fa-comment-o", path = "/views/dashboard/chat.html", active = true),
    new PortfolioTab(name = "Positions", icon = "fa-list-alt", path = "/views/dashboard/positions.html", active = false),
    new PortfolioTab(name = "Open Orders", icon = "fa-folder-open-o", path = "/views/dashboard/active_orders.html", active = false),
    new PortfolioTab(name = "Closed Orders", icon = "fa-folder-o", path = "/views/dashboard/closed_orders.html", active = false),
    new PortfolioTab(name = "Performance", icon = "fa-bar-chart-o", path = "/views/dashboard/performance.html", active = false),
    new PortfolioTab(name = "Exposure", icon = "fa-pie-chart", path = "/views/dashboard/exposure.html", active = false))

  /////////////////////////////////////////////////////////////////////
  //          Closed Order Functions
  /////////////////////////////////////////////////////////////////////

  $scope.getClosedOrders = () => {
    $scope.closedOrders.filter(_.accountType.contains($scope.getAccountType()))
  }

  $scope.isClosedOrderSelected = () => {
    $scope.closedOrders.nonEmpty && $scope.selectedClosedOrder.nonEmpty
  }

  $scope.selectClosedOrder = (closeOrder: js.UndefOr[Order]) => {
    $scope.selectedClosedOrder = closeOrder
  }

  $scope.toggleSelectedClosedOrder = () => {
    $scope.selectedClosedOrder = js.undefined
  }

  /////////////////////////////////////////////////////////////////////
  //          Active Order Functions
  /////////////////////////////////////////////////////////////////////

  $scope.cancelOrder = (aPortfolioId: js.UndefOr[String], anOrderId: js.UndefOr[String]) => {
    for {
      portfolioId <- aPortfolioId
      orderId <- anOrderId
    } {
      asyncLoading($scope)(portfolioService.cancelOrder(portfolioId, orderId)) onComplete {
        case Success(response) => $scope.$apply(() => $scope.portfolio = response.data)
        case Failure(err) =>
          toaster.error("Failed to cancel order")
          console.error(s"Failed to cancel order: ${err.displayMessage}")
      }
    }
  }

  $scope.computeOrderCost = (anOrder: js.UndefOr[Order]) => anOrder.flatMap(_.totalCost)

  $scope.getActiveOrders = () => {
    val orders = $scope.orders filter (_.accountType.contains($scope.getAccountType()))
    enrichOrders(orders)
    orders
  }

  $scope.isMarketOrder = (anOrder: js.UndefOr[Order]) => {
    anOrder.exists(order => order.priceType.exists(marketOrderTypes.contains))
  }

  $scope.isOrderSelected = () => $scope.getActiveOrders().nonEmpty && $scope.selectedOrder.nonEmpty

  $scope.popupNewOrderDialog = (aSymbol: js.UndefOr[String], anAccountType: js.UndefOr[String]) => {
    val promise = newOrderDialog.popup(new NewOrderParams(
      symbol = aSymbol,
      accountType = anAccountType
    ))
    promise onComplete {
      case Success(portfolio) => $scope.portfolio = portfolio
      case Failure(e) =>
        toaster.error("New Order", e.displayMessage)
    }
    promise
  }

  $scope.selectOrder = (order: js.UndefOr[Order]) => $scope.selectedOrder = order

  $scope.toggleSelectedOrder = () => $scope.selectedOrder = js.undefined

  /////////////////////////////////////////////////////////////////////
  //          Performance Functions
  /////////////////////////////////////////////////////////////////////

  $scope.getPerformance = () => $scope.performance

  $scope.isPerformanceSelected = () => $scope.getPerformance().nonEmpty && $scope.selectedPerformance.nonEmpty

  $scope.selectPerformance = (performance: js.UndefOr[Performance]) => $scope.selectedPerformance = performance

  $scope.toggleSelectedPerformance = () => $scope.selectedPerformance = js.undefined

  $scope.cost = (aTx: js.UndefOr[Performance]) => aTx.flatMap(_.totalCost)

  $scope.soldValue = (aTx: js.UndefOr[Performance]) => aTx.flatMap(_.totalSold)

  $scope.proceeds = (aTx: js.UndefOr[Performance]) => aTx.flatMap(_.proceeds)

  $scope.gainLoss = (aTx: js.UndefOr[Performance]) => aTx.flatMap(_.gainLoss)

  /////////////////////////////////////////////////////////////////////
  //          Position Functions
  /////////////////////////////////////////////////////////////////////

  $scope.getPositions = () => {
    val positions = $scope.positions filter (_.accountType.contains($scope.getAccountType()))
    enrichPositions(positions)
    positions
  }

  $scope.isPositionSelected = () => $scope.getPositions().nonEmpty && $scope.selectedPosition.nonEmpty

  $scope.selectPosition = (position: js.UndefOr[Position]) => $scope.selectedPosition = position

  $scope.sellPosition = (aSymbol: js.UndefOr[String], aQuantity: js.UndefOr[Double]) => {
    for {
      symbol <- aSymbol
      quantity <- aQuantity
    } yield newOrderDialog.popup(new NewOrderParams(symbol = symbol, quantity = quantity))
  }

  $scope.toggleSelectedPosition = () => $scope.selectedPosition = js.undefined

  $scope.tradingStart = () => new js.Date()

  // TODO get this from a service

  /////////////////////////////////////////////////////////////////////
  //          Private Functions
  /////////////////////////////////////////////////////////////////////

  private def enrichOrders(orders: js.Array[Order]) {
    orders foreach { order =>
      order.symbol foreach { symbol =>
        quoteCache.get(symbol) foreach (quote => order.lastTrade = quote.lastTrade)
      }
    }
  }

  private def enrichPositions(positions: js.Array[Position]) {
    positions foreach { position =>
      position.symbol foreach { symbol =>
        quoteCache.get(symbol) foreach { quote =>
          position.lastTrade = quote.lastTrade
          position.gainLossPct = for {
            cost <- position.totalCost
            lastTrade <- quote.lastTrade
            quantity <- position.quantity
          } yield 100 * (lastTrade * quantity - cost) / cost
        }
      }
    }
  }

  private def resetOrders() {
    $scope.selectedOrder = js.undefined
    $scope.selectedClosedOrder = js.undefined
  }

  private def resetPositions() {
    $scope.selectedPosition = js.undefined
    $scope.selectedPerformance = js.undefined
  }

  //////////////////////////////////////////////////////////////////////
  //              Watch Event Listeners
  //////////////////////////////////////////////////////////////////////

  $scope.onContestSelected { (_, contest) =>
    console.log(s"[Portfolio] Contest '${contest.name}' selected")
    resetOrders()
    resetPositions()
  }

  $scope.onContestUpdated { (_, contest) =>
    console.log(s"[Portfolio] Contest '${contest.name}' updated")
    resetOrders()
    resetPositions()
  }

  $scope.onOrderUpdated { (_, portfolioId) =>
    console.log(s"[Portfolio] Orders for Portfolio '$portfolioId' updated")
    resetOrders()
  }

  $scope.onParticipantUpdated { (_, participant) =>
    console.log(s"[Portfolio] Player '${participant.name}' updated")
    resetPositions()
  }

}

/**
 * Portfolio Controller Companion
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
object PortfolioController {

  /**
   * Portfolio Tab
   * @author Lawrence Daniels <lawrence.daniels@gmail.com>
   */
  class PortfolioTab(val name: String, val icon: String, val path: String, var active: Boolean = false) extends js.Object

}

/**
 * Portfolio Scope
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
@js.native
trait PortfolioScope extends RootScope with GlobalSelectedSymbolScope {
  // variables
  var portfolioTabs: js.Array[PortfolioTab] = js.native
  var selectedClosedOrder: js.UndefOr[Order] = js.native
  var selectedOrder: js.UndefOr[Order] = js.native
  var selectedPerformance: js.UndefOr[Performance] = js.native
  var selectedPosition: js.UndefOr[Position] = js.native

  // model variables
  var closedOrders: js.Array[Order] = js.native
  var orders: js.Array[Order] = js.native
  var performance: js.Array[Performance] = js.native
  var portfolios: js.Array[Portfolio] = js.native
  var positions: js.Array[Position] = js.native

  // closed order functions
  var getClosedOrders: js.Function0[js.Array[Order]] = js.native
  var isClosedOrderSelected: js.Function0[Boolean] = js.native
  var selectClosedOrder: js.Function1[js.UndefOr[Order], Unit] = js.native
  var toggleSelectedClosedOrder: js.Function0[Unit] = js.native

  // order functions
  var computeOrderCost: js.Function1[js.UndefOr[Order], js.UndefOr[Double]] = js.native
  var cancelOrder: js.Function2[js.UndefOr[String], js.UndefOr[String], Unit] = js.native
  var getActiveOrders: js.Function0[js.Array[Order]] = js.native
  var getAccountType: js.Function0[String] = js.native
  var isMarketOrder: js.Function1[js.UndefOr[Order], Boolean] = js.native
  var isOrderSelected: js.Function0[Boolean] = js.native
  var selectOrder: js.Function1[js.UndefOr[Order], Unit] = js.native
  var popupNewOrderDialog: js.Function2[js.UndefOr[String], js.UndefOr[String], js.Promise[NewOrderDialogResult]] = js.native
  var toggleSelectedOrder: js.Function0[Unit] = js.native

  // performance functions
  var cost: js.Function1[js.UndefOr[Performance], js.UndefOr[Double]] = js.native
  var gainLoss: js.Function1[js.UndefOr[Performance], js.UndefOr[Double]] = js.native
  var proceeds: js.Function1[js.UndefOr[Performance], js.UndefOr[Double]] = js.native
  var soldValue: js.Function1[js.UndefOr[Performance], js.UndefOr[Double]] = js.native
  var getPerformance: js.Function0[js.Array[Performance]] = js.native
  var isPerformanceSelected: js.Function0[Boolean] = js.native
  var selectPerformance: js.Function1[js.UndefOr[Performance], Unit] = js.native
  var toggleSelectedPerformance: js.Function0[Unit] = js.native

  // position functions
  var getPositions: js.Function0[js.Array[Position]] = js.native
  var isPositionSelected: js.Function0[Boolean] = js.native
  var selectPosition: js.Function1[js.UndefOr[Position], Unit] = js.native
  var sellPosition: js.Function2[js.UndefOr[String], js.UndefOr[Double], js.UndefOr[js.Promise[NewOrderDialogResult]]] = js.native
  var toggleSelectedPosition: js.Function0[Unit] = js.native
  var tradingStart: js.Function0[js.Date] = js.native

}


