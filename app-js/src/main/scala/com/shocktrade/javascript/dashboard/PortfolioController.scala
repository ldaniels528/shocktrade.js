package com.shocktrade.javascript.dashboard

import org.scalajs.angularjs.Timeout
import org.scalajs.angularjs.cookies.Cookies
import org.scalajs.angularjs.toaster.Toaster
import org.scalajs.nodejs.util.ScalaJsHelper._
import org.scalajs.angularjs.{Controller, Scope, injected}
import com.shocktrade.javascript.AppEvents._
import com.shocktrade.javascript.dialogs.NewOrderDialogController.NewOrderDialogResult
import com.shocktrade.javascript.dialogs.{NewOrderDialog, NewOrderParams}
import com.shocktrade.javascript.discover.DiscoverController
import com.shocktrade.javascript.models._
import com.shocktrade.javascript.{GlobalLoading, MySessionService}
import org.scalajs.dom.console

import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js
import scala.util.{Failure, Success}

/**
  * Portfolio Controller
  * @author lawrence.daniels@gmail.com
  */
class PortfolioController($scope: PortfolioScope, $cookies: Cookies, $timeout: Timeout, toaster: Toaster,
                          @injected("MySessionService") mySession: MySessionService,
                          @injected("ContestService") contestService: ContestService,
                          @injected("NewOrderDialog") newOrderDialog: NewOrderDialog)
  extends Controller with GlobalLoading {

  private val marketOrderTypes = js.Array("MARKET", "MARKET_ON_CLOSE")

  $scope.selectedClosedOrder = js.undefined
  $scope.selectedOrder = js.undefined
  $scope.selectedPosition = js.undefined

  $scope.portfolioTabs = js.Array(
    PortfolioTab(name = "Chat", icon = "fa-comment-o", path = "/assets/views/dashboard/chat.htm", active = false),
    PortfolioTab(name = "Positions", icon = "fa-list-alt", path = "/assets/views/dashboard/positions.htm", active = false),
    PortfolioTab(name = "Open Orders", icon = "fa-folder-open-o", path = "/assets/views/dashboard/orders_active.htm", active = false),
    PortfolioTab(name = "Closed Orders", icon = "fa-folder-o", path = "/assets/views/dashboard/orders_closed.htm", active = false),
    PortfolioTab(name = "Performance", icon = "fa-bar-chart-o", path = "/assets/views/dashboard/performance.htm", active = false),
    PortfolioTab(name = "Exposure", icon = "fa-pie-chart", path = "/assets/views/dashboard/exposure.htm", active = false))

  /////////////////////////////////////////////////////////////////////
  //          Closed Order Functions
  /////////////////////////////////////////////////////////////////////

  $scope.getClosedOrders = () => {
    mySession.getClosedOrders.filter(_.accountType == $scope.getAccountType())
  }

  $scope.isClosedOrderSelected = () => {
    $scope.getClosedOrders().nonEmpty && $scope.selectedClosedOrder.nonEmpty
  }

  $scope.selectClosedOrder = (closeOrder: js.UndefOr[ClosedOrder]) => {
    $scope.selectedClosedOrder = closeOrder
  }

  $scope.toggleSelectedClosedOrder = () => {
    $scope.selectedClosedOrder = js.undefined
  }

  /////////////////////////////////////////////////////////////////////
  //          Active Order Functions
  /////////////////////////////////////////////////////////////////////

  $scope.computeOrderCost = (anOrder: js.UndefOr[Order]) => {
    anOrder.map(o => o.price * o.quantity + o.commission)
  }

  $scope.cancelOrder = (aContestId: js.UndefOr[BSONObjectID], aPlayerId: js.UndefOr[BSONObjectID], anOrderId: js.UndefOr[BSONObjectID]) => {
    for {
      contestId <- aContestId
      playerId <- aPlayerId
      orderId <- anOrderId
    } {
      asyncLoading($scope)(contestService.deleteOrder(contestId, playerId, orderId)) onComplete {
        case Success(contest) => mySession.setContest(contest)
        case Failure(err) =>
          toaster.error("Failed to cancel order")
      }
    }
  }

  $scope.getActiveOrders = () => {
    mySession.getOrders filter (_.accountType == $scope.getAccountType())
  }

  $scope.isMarketOrder = (anOrder: js.UndefOr[Order]) => {
    anOrder.exists(order => marketOrderTypes.contains(order.priceType))
  }

  $scope.isOrderSelected = () => $scope.getActiveOrders().nonEmpty && $scope.selectedOrder.nonEmpty

  $scope.popupNewOrderDialog = (anAccountType: js.UndefOr[String]) => anAccountType map { accountType =>
    newOrderDialog.popup(NewOrderParams(
      symbol = $cookies.getOrElse(DiscoverController.LastSymbolCookie, "AAPL"),
      accountType = accountType
    ))
  }

  $scope.selectOrder = (order: js.UndefOr[Order]) => $scope.selectedOrder = order

  $scope.toggleSelectedOrder = () => $scope.selectedOrder = js.undefined

  /////////////////////////////////////////////////////////////////////
  //          Performance Functions
  /////////////////////////////////////////////////////////////////////

  $scope.getPerformance = () => mySession.getPerformance

  $scope.isPerformanceSelected = () => $scope.getPerformance().nonEmpty && $scope.selectedPerformance.nonEmpty

  $scope.selectPerformance = (performance: js.UndefOr[Performance]) => $scope.selectedPerformance = performance

  $scope.toggleSelectedPerformance = () => $scope.selectedPerformance = js.undefined

  $scope.cost = (aTx: js.UndefOr[Performance]) => aTx map { tx =>
    tx.pricePaid * tx.quantity + tx.commissions
  }

  $scope.soldValue = (aTx: js.UndefOr[Performance]) => aTx map { tx =>
    tx.priceSold * tx.quantity
  }

  $scope.proceeds = (tx: js.UndefOr[Performance]) => {
    for {
      soldValue <- $scope.soldValue(tx)
      cost <- $scope.cost(tx)
    } yield soldValue - cost
  }

  $scope.gainLoss = (tx: js.UndefOr[Performance]) => {
    for {
      proceeds <- $scope.proceeds(tx)
      cost <- $scope.cost(tx)
    } yield (proceeds / cost) * 100d
  }

  /////////////////////////////////////////////////////////////////////
  //          Position Functions
  /////////////////////////////////////////////////////////////////////

  $scope.getPositions = () => {
    mySession.participant foreach enrichPositions
    mySession.getPositions filter (_.accountType == $scope.getAccountType())
  }

  $scope.isPositionSelected = () => $scope.getPositions().nonEmpty && $scope.selectedPosition.nonEmpty

  $scope.selectPosition = (position: js.UndefOr[Position]) => $scope.selectedPosition = position

  $scope.sellPosition = (aSymbol: js.UndefOr[String], aQuantity: js.UndefOr[Double]) => {
    for {
      symbol <- aSymbol
      quantity <- aQuantity
    } yield newOrderDialog.popup(NewOrderParams(symbol = symbol, quantity = quantity))
  }

  $scope.toggleSelectedPosition = () => $scope.selectedPosition = js.undefined

  $scope.tradingStart = () => new js.Date()

  /////////////////////////////////////////////////////////////////////
  //          Private Functions
  /////////////////////////////////////////////////////////////////////

  private def enrichOrders(participant: Participant) {
    if (mySession.participant.nonEmpty) {
      if (!isDefined(participant.dynamic.enrichedOrders)) {
        participant.dynamic.enrichedOrders = true

        for {
          contestId <- mySession.getContestID
          playerId <- participant._id
        } {
          contestService.getEnrichedOrders(contestId, playerId) onComplete {
            case Success(enrichedOrders) => mySession.participant.foreach(_.orders = enrichedOrders)
            case Failure(e) =>
              toaster.error("Error!", "Error loading enriched orders")
          }
        }
      }
    }
  }

  private def enrichPositions(participant: Participant) {
    if (mySession.participant.nonEmpty) {
      if (!isDefined(participant.dynamic.enrichedPositions)) {
        participant._id foreach { playerId =>
          participant.dynamic.enrichedPositions = true
          for {
            contestId <- mySession.getContestID
          } {
            contestService.getEnrichedPositions(contestId, playerId) onComplete {
              case Success(enrichedPositions) => mySession.participant.foreach(_.positions = enrichedPositions)
              case Failure(e) => toaster.error("Error loading enriched positions")
            }
          }
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

  $scope.$on(ContestSelected, { (event: js.Dynamic, contest: js.Dynamic) =>
    console.log(s"[Portfolio] Contest '${contest.name}' selected")
    resetOrders()
    resetPositions()
  })

  $scope.$on(ContestUpdated, { (event: js.Dynamic, contest: js.Dynamic) =>
    console.log(s"[Portfolio] Contest '${contest.name}' updated")
    resetOrders()
    resetPositions()
  })

  $scope.$on(OrderUpdated, { (event: js.Dynamic, contest: js.Dynamic) =>
    console.log(s"[Portfolio] Orders for Contest '${contest.name}' updated")
    resetOrders()
  })

  $scope.$on(ParticipantUpdated, { (event: js.Dynamic, contest: js.Dynamic) =>
    console.log(s"[Portfolio] Orders for Contest '${contest.name}' updated")
    resetPositions()
  })

}

/**
  * Portfolio Scope
  * @author lawrence.daniels@gmail.com
  */
@js.native
trait PortfolioScope extends Scope {
  // variables
  var portfolioTabs: js.Array[PortfolioTab] = js.native
  var selectedClosedOrder: js.UndefOr[ClosedOrder] = js.native
  var selectedOrder: js.UndefOr[Order] = js.native
  var selectedPerformance: js.UndefOr[Performance] = js.native
  var selectedPosition: js.UndefOr[Position] = js.native

  // closed order functions
  var getClosedOrders: js.Function0[js.Array[ClosedOrder]] = js.native
  var isClosedOrderSelected: js.Function0[Boolean] = js.native
  var selectClosedOrder: js.Function1[js.UndefOr[ClosedOrder], Unit] = js.native
  var toggleSelectedClosedOrder: js.Function0[Unit] = js.native

  // order functions
  var computeOrderCost: js.Function1[js.UndefOr[Order], js.UndefOr[Double]] = js.native
  var cancelOrder: js.Function3[js.UndefOr[BSONObjectID], js.UndefOr[BSONObjectID], js.UndefOr[BSONObjectID], Unit] = js.native
  var getActiveOrders: js.Function0[js.Array[Order]] = js.native
  var getAccountType: js.Function0[String] = js.native
  var isMarketOrder: js.Function1[js.UndefOr[Order], Boolean] = js.native
  var isOrderSelected: js.Function0[Boolean] = js.native
  var selectOrder: js.Function1[js.UndefOr[Order], Unit] = js.native
  var popupNewOrderDialog: js.Function1[js.UndefOr[String], js.UndefOr[js.Promise[NewOrderDialogResult]]] = js.native
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

/**
  * Portfolio Tab
  * @author lawrence.daniels@gmail.com
  */
@js.native
trait PortfolioTab extends js.Object {
  var name: String = js.native
  var icon: String = js.native
  var path: String = js.native
  var active: Boolean = js.native
}

/**
  * Portfolio Tab Companion Object
  * @author lawrence.daniels@gmail.com
  */
object PortfolioTab {

  def apply(name: String, icon: String, path: String, active: Boolean = false) = {
    val tab = New[PortfolioTab]
    tab.name = name
    tab.icon = icon
    tab.path = path
    tab.active = active
    tab
  }

}

