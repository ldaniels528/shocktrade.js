package com.shocktrade.client.contest

import com.shocktrade.client.ScopeEvents._
import com.shocktrade.client.contest.DashboardController._
import com.shocktrade.client.dialogs.NewOrderDialogController.NewOrderParams
import com.shocktrade.client.dialogs.{InvitePlayerDialog, NewOrderDialog, PerksDialog}
import com.shocktrade.client.discover.MarketStatusService
import com.shocktrade.client.users.GameStateFactory
import com.shocktrade.client.users.GameStateFactory.{ContestScope, PortfolioScope}
import com.shocktrade.client.{USMarketsStatusSupportScope, _}
import com.shocktrade.common.models.contest.ContestRanking
import com.shocktrade.common.{AppConstants, Ok}
import io.scalajs.JSON
import io.scalajs.dom.html.browser.console
import io.scalajs.npm.angularjs.AngularJsHelper._
import io.scalajs.npm.angularjs.cookies.Cookies
import io.scalajs.npm.angularjs.http.HttpResponse
import io.scalajs.npm.angularjs.toaster.Toaster
import io.scalajs.npm.angularjs.{Controller, Interval, Timeout, injected}
import io.scalajs.util.DurationHelper._
import io.scalajs.util.JsUnderOrHelper._
import io.scalajs.util.PromiseHelper.Implicits._

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js
import scala.scalajs.js.JSConverters._
import scala.util.{Failure, Success}

/**
 * Dashboard Controller
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
case class DashboardController($scope: DashboardControllerScope, $routeParams: DashboardRouteParams,
                               $cookies: Cookies, $interval: Interval, $timeout: Timeout, toaster: Toaster,
                               @injected("ContestFactory") contestFactory: ContestFactory,
                               @injected("ContestService") contestService: ContestService,
                               @injected("GameStateFactory") gameState: GameStateFactory,
                               @injected("InvitePlayerDialog") invitePlayerDialog: InvitePlayerDialog,
                               @injected("MarketStatusService") marketStatusService: MarketStatusService,
                               @injected("NewOrderDialog") newOrderDialog: NewOrderDialog,
                               @injected("PerksDialog") perksDialog: PerksDialog,
                               @injected("PortfolioService") portfolioService: PortfolioService)
  extends Controller
    with GlobalLoading
    with GlobalSelectedSymbol[DashboardControllerScope]
    with USMarketsStatusSupport[DashboardControllerScope] {

  implicit private val scope: DashboardControllerScope = $scope

  $scope.maxPlayers = AppConstants.MaxPlayers

  $scope.portfolioTabs = js.Array(
    new PortfolioTab(name = "Analyze", icon = "fa-pie-chart", path = "/views/dashboard/charts.html"),
    new PortfolioTab(name = "Socialize", icon = "fa-comment-o", path = "/views/dashboard/chat.html"),
    new PortfolioTab(name = "My Orders", icon = "fa-ravelry", path = "/views/dashboard/orders.html"),
    new PortfolioTab(name = "My Portfolio", icon = "fa-list-alt", path = "/views/dashboard/positions.html"),
    new PortfolioTab(name = "My Awards", icon = "fa-gift", path = "/views/dashboard/awards.html"))

  // set the active tab
  $scope.portfolioTabs.headOption.foreach(_.active = true)

  /////////////////////////////////////////////////////////////////////
  //          Initialization Functions
  /////////////////////////////////////////////////////////////////////

  $interval(() => $scope.clock = clock, 1.second)

  $scope.initDash = () => {
    console.info(s"${getClass.getSimpleName} initializing...")
    initDash()
  }

  $scope.onUserProfileUpdated { (_, _) => initDash() }

  private def initDash(): Unit = {
    $scope.resetMarketStatus($routeParams.contestID)
    for (contestID <- $routeParams.contestID) refreshView(contestID, gameState.userID)
  }

  private def clock: js.Date = {
    val timeOffset = gameState.contest.flatMap(_.timeOffset).orZero
    new js.Date(js.Date.now() - timeOffset)
  }

  private def refreshView(contestID: String, aUserID: js.UndefOr[String]): Unit = {
    $timeout(() => $scope.isRefreshing = true, 0.millis)
    val outcome = for {
      contest <- contestFactory.findContest(contestID)
      portfolio_? <- aUserID.toOption match {
        case Some(userID) => contestFactory.findPortfolio(contestID, userID).map(c => Some(c))
        case None => Future.successful(None)
      }
    } yield (contest, portfolio_?)

    outcome onComplete { _ => $timeout(() => $scope.isRefreshing = false, 500.millis) }
    outcome onComplete {
      case Success((contest, portfolio_?)) =>
          if (contest.contestID != gameState.contest.flatMap(_.contestID)) gameState.contest = contest
          portfolio_? foreach { portfolio =>
            if (portfolio.portfolioID != gameState.portfolio.flatMap(_.portfolioID)) gameState.portfolio = portfolio
          }
      case Failure(e) => toaster.error("Error", e.displayMessage)
    }
  }

  /////////////////////////////////////////////////////////////////////
  //          Pop-up Dialog Functions
  /////////////////////////////////////////////////////////////////////

  $scope.popupInvitePlayer = (aContestID: js.UndefOr[String]) => {
    for (contestID <- aContestID ?? $routeParams.contestID) {
      invitePlayerDialog.popup(contestID) onComplete {
        case Success(_) => refreshView(contestID, gameState.userID)
        case Failure(e) =>
          if (e.getMessage != "cancel") {
            toaster.error("invite Player", e.displayMessage)
            e.printStackTrace()
          }
      }
    }
  }

  $scope.popupPerksDialog = (aContestID: js.UndefOr[String], aUserID: js.UndefOr[String]) => {
    for {
      contestID <- aContestID ?? $routeParams.contestID
      userID <- aUserID
    } {
      perksDialog.popup(contestID, userID) onComplete {
        case Success(_) => refreshView(contestID, userID)
        case Failure(e) =>
          if (e.getMessage != "cancel") {
            toaster.error("Perk Management", e.displayMessage)
            e.printStackTrace()
          }
      }
    }
  }

  $scope.popupNewOrderDialog = (aContestID: js.UndefOr[String], aUserID: js.UndefOr[String], aSymbol: js.UndefOr[String]) => {
    for {
      contestID <- aContestID ?? $routeParams.contestID
      userID <- aUserID
    } {
      newOrderDialog.popup(new NewOrderParams(contestID = contestID, userID = userID, symbol = aSymbol)) onComplete {
        case Success(result) =>
          console.log(s"result = ${JSON.stringify(result)}")
          refreshView(contestID, userID)
        case Failure(e) =>
          if (e.getMessage != "cancel") {
            toaster.error("Order Management", e.displayMessage)
            e.printStackTrace()
          }
      }
    }
  }

  /////////////////////////////////////////////////////////////////////
  //          Perk Functions
  /////////////////////////////////////////////////////////////////////

  $scope.hasPerk = (aPerkCode: js.UndefOr[String]) => false // aPerkCode.exists(???)

  /////////////////////////////////////////////////////////////////////
  //          Contest Ranking Functions
  /////////////////////////////////////////////////////////////////////

  $scope.getJoiningPlayerRank = (aPlayerName: js.UndefOr[String], aTotalEquity: js.UndefOr[Double]) => {
    for {
      playerName <- aPlayerName
      totalEquity <- aTotalEquity
      rankings <- $scope.contest.flatMap(_.rankings)
      joiningRank <- getJoiningPlayerRank(playerName, totalEquity, rankings)
    } yield joiningRank
  }

  $scope.getRankCellClass = (aRanking: js.UndefOr[String]) => aRanking map {
    case rank if Set("1st", "2nd", "3rd").contains(rank) => s"rank_cell_$rank"
    case rank if rank == "join" => "rank_cell_join"
    case _ => "rank_cell"
  }

  $scope.isRankingsShown = () => !$scope.rankingsHidden.isTrue

  $scope.playerRankingOnTop = (aUserID: js.UndefOr[String], aRankings: js.UndefOr[js.Array[ContestRanking]]) => {
    val reorderedRankings = for {userID <- aUserID; rankings <- aRankings.map(playerRankingOnTop(userID, _))} yield rankings
    reorderedRankings ?? aRankings
  }

  $scope.toggleRankingsShown = () => $scope.rankingsHidden = !$scope.rankingsHidden.isTrue

  private def getJoiningPlayerRank(playerName: String, totalEquity: Double, rankings: js.Array[ContestRanking]): js.UndefOr[String] = {
    val tempRankings = ContestRanking(username = playerName, totalEquity = totalEquity) :: rankings.toList
    ContestRanking.computeRankings(tempRankings)
      .find(_.username.contains(playerName)).orUndefined
      .flatMap(_.rank)
  }

  private def playerRankingOnTop(userID: String, rankings: js.Array[ContestRanking]): js.Array[ContestRanking] = {
    val list = rankings.toList.sortBy(_.rankNum.getOrElse(Int.MaxValue))
    (list.filter(_.userID.contains(userID)) ::: list.filterNot(_.userID.contains(userID))).toJSArray
  }

  /////////////////////////////////////////////////////////////////////
  //          Contest Management Functions
  /////////////////////////////////////////////////////////////////////

  $scope.deleteContest = (aContestID: js.UndefOr[String]) => aContestID.map(deleteContest)

  $scope.joinContest = (aContestID: js.UndefOr[String]) => {
    for {contestID <- aContestID; userID <- gameState.userID} yield joinContest(contestID, userID)
  }

  $scope.quitContest = (aContestID: js.UndefOr[String]) => {
    for {contestID <- aContestID; userID <- gameState.userID} yield quitContest(contestID, userID)
  }

  $scope.startContest = (aContestID: js.UndefOr[String]) => aContestID.map(startContest)

  private def deleteContest(contestID: String): js.Promise[HttpResponse[Ok]] = {
    $scope.isDeleting = true
    val outcome = contestService.deleteContest(contestID)
    outcome onComplete {
      case Success(response) =>
        console.log(s"response = ${JSON.stringify(response.data)}")
        $scope.initDash()
        $timeout(() => $scope.isDeleting = false, 0.5.seconds)
      case Failure(e) =>
        toaster.error("Error!", "Failed to delete contest")
        console.error("An error occurred while deleting the contest")
          $timeout(() => $scope.isDeleting = false, 0.5.seconds)
      }
      outcome
  }

  private def joinContest(contestID: String, userID: String): js.Promise[HttpResponse[Ok]] = {
    $scope.isJoining = true
    val outcome = contestService.joinContest(contestID, userID)
    outcome onComplete {
      case Success(response) =>
        console.info(s"response = ${JSON.stringify(response.data)}")
        $scope.initDash()
        $timeout(() => $scope.isJoining = false, 0.5.seconds)
      case Failure(e) =>
        toaster.error(title = "Error!", body = "Failed to join contest")
        console.error("An error occurred while joining the contest")
        $timeout(() => $scope.isJoining = false, 0.5.seconds)
    }
    outcome
  }

  private def quitContest(contestID: String, userID: String): js.Promise[HttpResponse[Ok]] = {
    $scope.isQuiting = true
    val outcome = contestService.quitContest(contestID, userID)
    outcome onComplete {
      case Success(response) =>
        console.info(s"response = ${JSON.stringify(response.data)}")
        $scope.initDash()
        $timeout(() => $scope.isQuiting = false, 0.5.seconds)
      case Failure(e) =>
        toaster.error(title = "Error!", e.displayMessage)
        console.error("An error occurred while joining the contest")
        $timeout(() => $scope.isQuiting = false, 0.5.seconds)
    }
    outcome
  }

  private def startContest(contestID: String): js.Promise[HttpResponse[Ok]] = {
    $scope.isStarting = true
    val outcome = contestService.startContest(contestID)
    outcome onComplete {
      case Success(response) =>
        console.info(s"response = ${JSON.stringify(response.data)}")
        $scope.initDash()
        $timeout(() => $scope.isStarting = false, 0.5.seconds)
      case Failure(e) =>
        toaster.error("An error occurred while starting the contest")
        console.error(s"Error starting contest: ${e.getMessage}")
        $timeout(() => $scope.isStarting = false, 0.5.seconds)
    }
    outcome
  }

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
  trait DashboardControllerScope extends RootScope
    with ContestScope
    with GlobalNavigation
    with GlobalSelectedSymbolScope
    with PortfolioScope
    with USMarketsStatusSupportScope {

    // functions
    var initDash: js.Function0[Unit] = js.native
    var getJoiningPlayerRank: js.Function2[js.UndefOr[String], js.UndefOr[Double], js.UndefOr[String]] = js.native
    var getRankCellClass: js.Function1[js.UndefOr[String], js.UndefOr[String]] = js.native
    var hasPerk: js.Function1[js.UndefOr[String], Boolean] = js.native
    var isRankingsShown: js.Function0[Boolean] = js.native
    var playerRankingOnTop: js.Function2[js.UndefOr[String], js.UndefOr[js.Array[ContestRanking]], js.UndefOr[js.Array[ContestRanking]]] = js.native
    var toggleRankingsShown: js.Function0[Unit] = js.native

    // popup dialog functions
    var popupInvitePlayer: js.Function1[js.UndefOr[String], Unit] = js.native
    var popupNewOrderDialog: js.Function3[js.UndefOr[String], js.UndefOr[String], js.UndefOr[String], Unit] = js.native
    var popupPerksDialog: js.Function2[js.UndefOr[String], js.UndefOr[String], Unit] = js.native

    // contest management functions
    var deleteContest: js.Function1[js.UndefOr[String], js.UndefOr[js.Promise[HttpResponse[Ok]]]] = js.native
    var joinContest: js.Function1[js.UndefOr[String], js.UndefOr[js.Promise[HttpResponse[Ok]]]] = js.native
    var quitContest: js.Function1[js.UndefOr[String], js.UndefOr[js.Promise[HttpResponse[Ok]]]] = js.native
    var startContest: js.Function1[js.UndefOr[String], js.UndefOr[js.Promise[HttpResponse[Ok]]]] = js.native

    // contest management variables
    var isJoining: js.UndefOr[Boolean] = js.native
    var isQuiting: js.UndefOr[Boolean] = js.native
    var isRefreshing: js.UndefOr[Boolean] = js.native
    var isStarting: js.UndefOr[Boolean] = js.native
    var isDeleting: js.UndefOr[Boolean] = js.native

    // variables
    var clock: js.UndefOr[js.Date] = js.native
    //var contest: js.UndefOr[Contest] = js.native
    //var portfolio: js.UndefOr[Portfolio] = js.native
    var maxPlayers: js.UndefOr[Int] = js.native
    var portfolioTabs: js.Array[PortfolioTab] = js.native
    var rankingsHidden: js.UndefOr[Boolean] = js.native

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