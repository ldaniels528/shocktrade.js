package com.shocktrade.javascript.dashboard

import com.ldaniels528.scalascript.ScalaJsHelper._
import com.ldaniels528.scalascript.core.Timeout
import com.ldaniels528.scalascript.extensions.{Cookies, Toaster}
import com.ldaniels528.scalascript.{Controller, injected}
import com.shocktrade.javascript.AppEvents._
import com.shocktrade.javascript.{GlobalLoading, MySession}
import com.shocktrade.javascript.dashboard.PortfolioController._
import com.shocktrade.javascript.dialogs.NewOrderDialogService
import com.shocktrade.javascript.discover.DiscoverController

import scala.scalajs.concurrent.JSExecutionContext.Implicits.runNow
import scala.scalajs.js
import scala.scalajs.js.Dynamic.{global => g, literal => JS}
import scala.util.{Failure, Success}

/**
 * Portfolio Controller
 * @author lawrence.daniels@gmail.com
 */
class PortfolioController($scope: js.Dynamic, $cookies: Cookies, $timeout: Timeout, toaster: Toaster,
                          @injected("MySession") mySession: MySession,
                          @injected("ContestService") contestService: ContestService,
                          @injected("NewOrderDialog") newOrderDialog: NewOrderDialogService)
  extends Controller with GlobalLoading {

  $scope.selectedClosedOrder = null
  $scope.selectedOrder = null
  $scope.selectedPosition = null

  $scope.portfolioTabs = portfolioTabs

  /////////////////////////////////////////////////////////////////////
  //          Active Order Functions
  /////////////////////////////////////////////////////////////////////

  $scope.getActiveOrders = () => getActiveOrders

  $scope.cancelOrder = (contestId: String, playerId: String, orderId: String) => cancelOrder(contestId, playerId, orderId)

  $scope.isMarketOrder = (order: js.Dynamic) => order.priceType === "MARKET" || order.priceType === "MARKET_ON_CLOSE"

  $scope.isOrderSelected = () => getActiveOrders.nonEmpty && selectedOrder.nonEmpty

  $scope.selectOrder = (order: js.Dynamic) => $scope.selectedOrder = order

  $scope.toggleSelectedOrder = () => $scope.selectedOrder = null

  $scope.popupNewOrderDialog = (accountType: js.UndefOr[String]) => popupNewOrderDialog(accountType)

  private def cancelOrder(contestId: String, playerId: String, orderId: String) = {
    asyncLoading($scope)(contestService.deleteOrder(contestId, playerId, orderId)) onComplete {
      case Success(contest) => mySession.setContest(contest)
      case Failure(err) =>
        toaster.error("Failed to cancel order")
    }
  }

  private def popupNewOrderDialog(accountType: js.UndefOr[String]) = {
    newOrderDialog.popup(JS(
      symbol = $cookies.getOrElse(DiscoverController.LastSymbolCookie, "AAPL"),
      accountType = accountType
    ))
  }

  private def getActiveOrders = mySession.getOrders() filter (_.accountType === $scope.getAccountType())

  private def selectedOrder = Option($scope.selectedOrder) map (_.as[js.Dynamic])

  /////////////////////////////////////////////////////////////////////
  //          Closed Order Functions
  /////////////////////////////////////////////////////////////////////

  $scope.getClosedOrders = () => getClosedOrders

  $scope.isClosedOrderSelected = () => getClosedOrders.nonEmpty && selectedClosedOrder.nonEmpty

  $scope.selectClosedOrder = (closeOrder: js.Dynamic) => $scope.selectedClosedOrder = closeOrder

  $scope.toggleSelectedClosedOrder = () => $scope.selectedClosedOrder = null

  $scope.orderCost = (o: js.Dynamic) => o.price * o.quantity + o.commission

  private def getClosedOrders = mySession.getClosedOrders().filter(_.accountType === $scope.getAccountType())

  private def selectedClosedOrder = Option($scope.selectedClosedOrder) map (_.as[js.Dynamic])

  /////////////////////////////////////////////////////////////////////
  //          Performance Functions
  /////////////////////////////////////////////////////////////////////

  $scope.getPerformance = () => getPerformance

  $scope.isPerformanceSelected = () => getPerformance.nonEmpty && isDefined($scope.selectedPerformance)

  $scope.selectPerformance = (performance: js.Dynamic) => $scope.selectedPerformance = performance

  $scope.toggleSelectedPerformance = () => $scope.selectedPerformance = null

  $scope.cost = (tx: js.Dynamic) => cost(tx)

  $scope.soldValue = (tx: js.Dynamic) => soldValue(tx)

  $scope.proceeds = (tx: js.Dynamic) => proceeds(tx)

  $scope.gainLoss = (tx: js.Dynamic) => (proceeds(tx) / cost(tx)) * 100d

  private def cost(tx: js.Dynamic): Double = tx.pricePaid.as[Double] * tx.quantity.as[Double] + tx.commissions.as[Double]

  private def getPerformance = mySession.getPerformance()

  private def proceeds(tx: js.Dynamic): Double = soldValue(tx) - cost(tx)

  private def soldValue(tx: js.Dynamic): Double = tx.priceSold.as[Double] * tx.quantity.as[Double]

  /////////////////////////////////////////////////////////////////////
  //          Position Functions
  /////////////////////////////////////////////////////////////////////

  $scope.getPositions = () => getPositions

  $scope.isPositionSelected = () => getPositions.nonEmpty && isDefined($scope.selectedPosition)

  $scope.selectPosition = (position: js.Dynamic) => $scope.selectedPosition = position

  $scope.sellPosition = (symbol: js.UndefOr[String], quantity: js.UndefOr[Int]) => {
    newOrderDialog.popup(JS(symbol = symbol, quantity = quantity))
  }

  $scope.toggleSelectedPosition = () => $scope.selectedPosition = null

  $scope.tradingStart = () => new js.Date()

  // TODO replace with service call

  private def getPositions = {
    mySession.participant foreach enrichPositions
    mySession.getPositions() filter (_.accountType === $scope.getAccountType())
  }

  /////////////////////////////////////////////////////////////////////
  //          Private Functions
  /////////////////////////////////////////////////////////////////////

  private def enrichOrders(participant: js.Dynamic) {
    if (!mySession.participantIsEmpty()) {
      if (!isDefined(participant.enrichedOrders)) {
        participant.enrichedOrders = true
        contestService.getEnrichedOrders(mySession.getContestID(), participant.OID) onComplete {
          case Success(enrichedOrders) => mySession.getParticipant().orders = enrichedOrders
          case Failure(e) =>
            toaster.error("Error!", "Error loading enriched orders")
        }
      }
    }
  }

  private def enrichPositions(participant: js.Dynamic) {
    if (!mySession.participantIsEmpty()) {
      if (!isDefined(participant.enrichedPositions)) {
        participant.enrichedPositions = true
        contestService.getEnrichedPositions(mySession.getContestID(), participant.OID) onComplete {
          case Success(enrichedPositions) => mySession.getParticipant().positions = enrichedPositions
          case Failure(e) => toaster.error("Error loading enriched positions")
        }
      }
    }
  }

  private def resetOrders() {
    $scope.selectedOrder = null
    $scope.selectedClosedOrder = null
  }

  private def resetPositions() {
    $scope.selectedPosition = null
    $scope.selectedPerformance = null
  }

  //////////////////////////////////////////////////////////////////////
  //              Watch Event Listeners
  //////////////////////////////////////////////////////////////////////

  $scope.$on(ContestSelected, { (event: js.Dynamic, contest: js.Dynamic) =>
    g.console.log(s"[Portfolio] Contest '${contest.name}' selected")
    resetOrders()
    resetPositions()
  })

  $scope.$on(ContestUpdated, { (event: js.Dynamic, contest: js.Dynamic) =>
    g.console.log(s"[Portfolio] Contest '${contest.name}' updated")
    resetOrders()
    resetPositions()
  })

  $scope.$on(OrderUpdated, { (event: js.Dynamic, contest: js.Dynamic) =>
    g.console.log(s"[Portfolio] Orders for Contest '${contest.name}' updated")
    resetOrders()
  })

  $scope.$on(ParticipantUpdated, { (event: js.Dynamic, contest: js.Dynamic) =>
    g.console.log(s"[Portfolio] Orders for Contest '${contest.name}' updated")
    resetPositions()
  })

}

/**
 * Portfolio Controller Singleton
 * @author lawrence.daniels@gmail.com
 */
object PortfolioController {

  private val portfolioTabs = js.Array(
    JS(name = "Chat", icon = "fa-comment-o", path = "/assets/views/dashboard/chat.htm", active = false),
    JS(name = "Positions", icon = "fa-list-alt", path = "/assets/views/dashboard/positions.htm", active = false),
    JS(name = "Open Orders", icon = "fa-folder-open-o", path = "/assets/views/dashboard/orders_active.htm", active = false),
    JS(name = "Closed Orders", icon = "fa-folder-o", path = "/assets/views/dashboard/orders_closed.htm", active = false),
    JS(name = "Performance", icon = "fa-bar-chart-o", path = "/assets/views/dashboard/performance.htm", active = false),
    JS(name = "Exposure", icon = "fa-pie-chart", path = "/assets/views/dashboard/exposure.htm", active = false))

}
