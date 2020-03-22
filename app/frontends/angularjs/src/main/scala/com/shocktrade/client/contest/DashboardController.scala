package com.shocktrade.client.contest

import com.shocktrade.client.ScopeEvents._
import com.shocktrade.client._
import com.shocktrade.client.contest.DashboardController._
import com.shocktrade.client.dialogs.NewOrderDialogController.{NewOrderDialogResult, NewOrderParams}
import com.shocktrade.client.dialogs.{NewOrderDialog, PerksDialog}
import com.shocktrade.client.models.contest.Performance
import com.shocktrade.client.users.GameStateFactory
import com.shocktrade.client.users.GameStateFactory.{ContestScope, PortfolioScope}
import io.scalajs.JSON
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
 * Dashboard Controller
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
case class DashboardController($scope: DashboardControllerScope, $routeParams: DashboardRouteParams,
                               $cookies: Cookies,  $timeout: Timeout, toaster: Toaster,
                               @injected("ContestFactory") contestFactory: ContestFactory,
                               @injected("ContestService") contestService: ContestService,
                               @injected("GameStateFactory") gameState: GameStateFactory,
                               @injected("NewOrderDialog") newOrderDialog: NewOrderDialog,
                               @injected("PerksDialog") perksDialog: PerksDialog,
                               @injected("PortfolioService") portfolioService: PortfolioService)
  extends Controller with GlobalLoading with GlobalSelectedSymbol[DashboardControllerScope] {

  implicit private val scope: DashboardControllerScope = $scope

  $scope.portfolioTabs = js.Array(
    new PortfolioTab(name = "Chat", icon = "fa-comment-o", path = "/views/dashboard/chat.html", active = true),
    new PortfolioTab(name = "Positions", icon = "fa-list-alt", path = "/views/dashboard/positions.html", active = false),
    new PortfolioTab(name = "Open Orders", icon = "fa-folder-open-o", path = "/views/dashboard/active_orders.html", active = false),
    new PortfolioTab(name = "Closed Orders", icon = "fa-folder-o", path = "/views/dashboard/closed_orders.html", active = false),
    new PortfolioTab(name = "Performance", icon = "fa-bar-chart-o", path = "/views/dashboard/performance.html", active = false),
    new PortfolioTab(name = "Exposure", icon = "fa-pie-chart", path = "/views/dashboard/exposure.html", active = false))

  /////////////////////////////////////////////////////////////////////
  //          Initialization Functions
  /////////////////////////////////////////////////////////////////////

  $scope.initDash = () => {
    console.info(s"${getClass.getSimpleName} initializing... routeParams = ${JSON.stringify($routeParams)}")
    for (contestID <- $routeParams.contestID) {
      loadContest(contestID)
      gameState.userID.foreach(loadPortfolio(contestID, _))
    }
  }

  $scope.onUserProfileUpdated { (_, _) => $scope.initDash() }

  private def loadContest(contestID: String): Unit = {
    contestFactory.findContest(contestID) onComplete {
      case Success(contest) =>
        if (contest.contestID != gameState.contest.flatMap(_.contestID)) $scope.$apply { () => gameState.contest = contest }
      case Failure(e) => toaster.error("Error", e.displayMessage)
    }
  }

  private def loadPortfolio(contestID: String, userID: String): Unit = {
    contestFactory.findPortfolio(contestID, userID) onComplete {
      case Success(portfolio) =>
        if (portfolio.portfolioID != gameState.portfolio.flatMap(_.portfolioID)) $scope.$apply { () => gameState.portfolio = portfolio }
      case Failure(e) => toaster.error("Error", "Failed to load portfolio")
    }
  }

  /////////////////////////////////////////////////////////////////////
  //          Pop-up Dialog Functions
  /////////////////////////////////////////////////////////////////////

  $scope.popupPerksDialog = (aContestID: js.UndefOr[String], aUserID: js.UndefOr[String]) => {
    for {
      contestID <- aContestID ?? $routeParams.contestID
      userID <- aUserID
    } {
      perksDialog.popup(contestID, userID) onComplete {
        case Success(_) => loadContest(contestID)
        case Failure(e) =>
          if (e.getMessage != "cancel") {
            toaster.error("Perk Management", e.displayMessage)
            e.printStackTrace()
          }
      }
    }
  }

  $scope.popupNewOrderDialog = (aSymbol: js.UndefOr[String]) => {
    val promise = newOrderDialog.popup(new NewOrderParams(symbol = aSymbol))
    promise onComplete {
      case Success(result) => console.log(s"result = ${JSON.stringify(result)}")
      case Failure(e) =>
        if (e.getMessage != "cancel") {
          toaster.error("New Order", e.displayMessage)
          e.printStackTrace()
        }
    }
    promise
  }

  /////////////////////////////////////////////////////////////////////
  //          Performance Functions
  /////////////////////////////////////////////////////////////////////

  $scope.getPerformance = () => $scope.portfolio.flatMap(_.performance)

  $scope.isPerformanceSelected = () => $scope.getPerformance().nonEmpty && $scope.selectedPerformance.nonEmpty

  $scope.selectPerformance = (performance: js.UndefOr[Performance]) => $scope.selectedPerformance = performance

  $scope.toggleSelectedPerformance = () => $scope.selectedPerformance = js.undefined

  $scope.cost = (aTx: js.UndefOr[Performance]) => aTx.flatMap(_.totalCost)

  $scope.soldValue = (aTx: js.UndefOr[Performance]) => aTx.flatMap(_.totalSold)

  $scope.proceeds = (aTx: js.UndefOr[Performance]) => aTx.flatMap(_.proceeds)

  $scope.gainLoss = (aTx: js.UndefOr[Performance]) => aTx.flatMap(_.gainLoss)

  /////////////////////////////////////////////////////////////////////
  //          Other Functions
  /////////////////////////////////////////////////////////////////////

  $scope.getRankCellClass = (aRanking: js.UndefOr[String]) => aRanking map {
    case rank if Set("1st", "2nd", "3rd").contains(rank) => s"rank_cell_$rank"
    case _ => "rank_cell"
  }

  $scope.hasPerk = (aPerkCode: js.UndefOr[String]) => false // aPerkCode.exists(???)

  $scope.isRankingsShown = () => !$scope.rankingsHidden.isTrue

  $scope.toggleRankingsShown = () => $scope.rankingsHidden = !$scope.rankingsHidden.isTrue

}

/**
 * Dashboard Controller Companion
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
object DashboardController {

  /**
   * Dashboard Controller Scope
   * @author Lawrence Daniels <lawrence.daniels@gmail.com>
   */
  @js.native
  trait DashboardControllerScope extends RootScope with ContestScope with GlobalNavigation with GlobalSelectedSymbolScope with PortfolioScope {
    // functions
    var initDash: js.Function0[Unit] = js.native
    var getRankCellClass: js.Function1[js.UndefOr[String], js.UndefOr[String]] = js.native
    var getPerformance: js.Function0[js.UndefOr[js.Array[Performance]]] = js.native
    var isPerformanceSelected: js.Function0[Boolean] = js.native
    var isRankingsShown: js.Function0[Boolean] = js.native
    var popupNewOrderDialog: js.Function1[js.UndefOr[String], js.Promise[NewOrderDialogResult]] = js.native
    var popupPerksDialog: js.Function2[js.UndefOr[String], js.UndefOr[String], Unit] = js.native
    var selectPerformance: js.Function1[js.UndefOr[Performance], Unit] = js.native
    var toggleSelectedPerformance: js.Function0[Unit] = js.native
    var toggleRankingsShown: js.Function0[Unit] = js.native

    // financial functions
    var cost: js.Function1[js.UndefOr[Performance], js.UndefOr[Double]] = js.native
    var gainLoss: js.Function1[js.UndefOr[Performance], js.UndefOr[Double]] = js.native
    var proceeds: js.Function1[js.UndefOr[Performance], js.UndefOr[Double]] = js.native
    var soldValue: js.Function1[js.UndefOr[Performance], js.UndefOr[Double]] = js.native

    // variables
    //var contest: js.UndefOr[Contest] = js.native
    var hasPerk: js.Function1[js.UndefOr[String], Boolean] = js.native
    //var portfolio: js.UndefOr[Portfolio] = js.native
    var portfolioTabs: js.Array[PortfolioTab] = js.native
    var rankingsHidden: js.UndefOr[Boolean] = js.native
    var selectedPerformance: js.UndefOr[Performance] = js.native

  }

  /**
   * Dashboard Route Params
   * @author Lawrence Daniels <lawrence.daniels@gmail.com>
   */
  @js.native
  trait DashboardRouteParams extends js.Object {
    var contestID: js.UndefOr[String] = js.native

  }

  /**
   * Portfolio Tab
   * @author Lawrence Daniels <lawrence.daniels@gmail.com>
   */
  class PortfolioTab(val name: String, val icon: String, val path: String, var active: Boolean = false) extends js.Object

}