package com.shocktrade.client.contest

import com.shocktrade.client.GameState._
import com.shocktrade.client.ScopeEvents._
import com.shocktrade.client.contest.DashboardController._
import com.shocktrade.client.contest.PerksDialog._
import com.shocktrade.client.dialogs.InvitePlayerDialogController.InvitePlayerDialogResult
import com.shocktrade.client.dialogs.NewOrderDialogController.{NewOrderDialogResult, NewOrderParams}
import com.shocktrade.client.dialogs.{InvitePlayerDialog, NewOrderDialog, PlayerProfileDialog}
import com.shocktrade.client.discover.MarketStatusService
import com.shocktrade.client.users.UserService
import com.shocktrade.client.{USMarketsStatusSupportScope, _}
import com.shocktrade.common.models.contest.{Contest, ContestRanking, Portfolio}
import com.shocktrade.common.{AppConstants, Ok}
import io.scalajs.JSON
import io.scalajs.dom.html.browser.{Window, console}
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
                               $cookies: Cookies, $interval: Interval, $timeout: Timeout, toaster: Toaster, $window: Window,
                               @injected("ContestFactory") contestFactory: ContestFactory,
                               @injected("ContestService") contestService: ContestService,
                               @injected("InvitePlayerDialog") invitePlayerDialog: InvitePlayerDialog,
                               @injected("MarketStatusService") marketStatusService: MarketStatusService,
                               @injected("NewOrderDialog") newOrderDialog: NewOrderDialog,
                               @injected("PerksDialog") perksDialog: PerksDialog,
                               @injected("PlayerProfileDialog") playerProfileDialog: PlayerProfileDialog,
                               @injected("PortfolioService") portfolioService: PortfolioService,
                               @injected("UserService") userService: UserService)
  extends Controller
    with AwardsSupport
    with ContestCssSupport
    with GlobalLoading
    with GlobalSelectedSymbol
    with PlayerProfilePopupSupport
    with USMarketsStatusSupport {

  // refresh the dashboard every minute
  $interval(() => initDash(), 5.minute)

  $scope.maxPlayers = AppConstants.MaxPlayers

  private val _userID = $cookies.getGameState.userID

  private val portfolioTabs = js.Array(
    new PortfolioTab(name = "Socialize", icon = "fa-comment-o", path = "/views/dashboard/chat.html"),
    new PortfolioTab(name = "Analyze", icon = "fa-pie-chart", path = "/views/dashboard/charts.html", isAuthRequired = true),
    new PortfolioTab(name = "My Orders", icon = "fa-list-alt", path = "/views/dashboard/orders.html", isAuthRequired = true),
    new PortfolioTab(name = "My Holdings", icon = "fa-ravelry", path = "/views/dashboard/holdings.html", isAuthRequired = true),
    new PortfolioTab(name = "My Awards", icon = "fa-trophy", path = "/views/dashboard/awards.html", isAuthRequired = true),
    new PortfolioTab(name = "My Perks", icon = "fa-gift", path = "/views/dashboard/perks.html", isAuthRequired = true))

  // select the first tab
  portfolioTabs.zipWithIndex foreach { case (tab, index) => tab.active = index == 0 }

  $scope.getPortfolioTabs = () => portfolioTabs.filter(tab => !tab.isAuthRequired || !$scope.contest.toOption.flatMap(_.status.toOption).contains("CLOSED"))

  $scope.isParticipant = () => _userID.flatMap(isParticipant)

  private def isParticipant(userID: String): js.UndefOr[Boolean] = {
    for {
      contest <- $scope.contest
      rankings <- contest.rankings
      participants = rankings.flatMap(_.userID.toOption)
    } yield participants.contains(userID)
  }

  /////////////////////////////////////////////////////////////////////
  //          Initialization Functions
  /////////////////////////////////////////////////////////////////////

  $interval(() => $scope.clock = clock, 1.second)

  $scope.initDash = () => {
    console.info(s"${getClass.getSimpleName} initializing...")
    initDash()
  }

  $scope.onUserProfileUpdated { (_, _) => initDash() }

  private def initDash(): js.UndefOr[js.Promise[(Contest, Option[Portfolio])]] = {
    $scope.resetMarketStatus($routeParams.contestID)
    for (contestID <- $routeParams.contestID) yield refreshView(contestID, _userID)
  }

  private def clock: js.Date = {
    val timeOffset = $scope.contest.flatMap(_.timeOffset).orZero
    new js.Date(js.Date.now() - timeOffset)
  }

  private def refreshView(contestID: String, aUserID: js.UndefOr[String]): js.Promise[(Contest, Option[Portfolio])] = {
    $timeout(() => $scope.isRefreshing = true, 0.millis)
    val outcome = (for {
      contest <- contestFactory.findContest(contestID)
      portfolio_? <- aUserID.toOption match {
        case Some(userID) => contestFactory.findOptionalPortfolio(contestID, userID)
        case None => Future.successful(None)
      }
    } yield (contest, portfolio_?)).toJSPromise

    outcome onComplete { _ => $timeout(() => $scope.isRefreshing = false, 500.millis) }
    outcome onComplete {
      case Success((contest, portfolio_?)) =>
        $scope.$apply { () =>
          $scope.contest = contest
          $scope.portfolio = portfolio_?.orUndefined
        }
      case Failure(e) => toaster.error("Error", e.displayMessage)
        console.error(s"error: ${e.getMessage} | ${JSON.stringify(e.asInstanceOf[js.Any])}")
        e.printStackTrace()
    }
    outcome
  }

  /////////////////////////////////////////////////////////////////////
  //          Pop-up Dialog Functions
  /////////////////////////////////////////////////////////////////////

  $scope.popupInvitePlayer = (aContestID: js.UndefOr[String]) => {
    for (contestID <- aContestID ?? $routeParams.contestID) yield popupInvitePlayer(contestID)
  }

  $scope.popupPerksDialog = (aContestID: js.UndefOr[String], aUserID: js.UndefOr[String]) => {
    for {
      contestID <- aContestID ?? $routeParams.contestID
      userID <- aUserID
    } yield popupPerksDialog(contestID, userID)
  }

  $scope.popupNewOrderDialog = (aContestID: js.UndefOr[String], aUserID: js.UndefOr[String], aSymbol: js.UndefOr[String]) => {
    for {
      contestID <- aContestID ?? $routeParams.contestID
      userID <- aUserID
    } yield popupNewOrderDialog(contestID, userID, aSymbol)
  }

  private def popupInvitePlayer(contestID: String): js.Promise[InvitePlayerDialogResult] = {
    val outcome = invitePlayerDialog.popup(contestID)
    outcome onComplete {
      case Success(_) => refreshView(contestID, _userID)
      case Failure(e) =>
        if (e.getMessage != "cancel") {
          toaster.error("invite Player", e.displayMessage)
          e.printStackTrace()
        }
    }
    outcome
  }

  private def popupPerksDialog(contestID: String, userID: String): js.Promise[PerksDialogResult] = {
    val outcome = perksDialog.popup(contestID, userID)
    outcome onComplete {
      case Success(_) => refreshView(contestID, userID)
      case Failure(e) =>
        if (e.getMessage != "cancel") {
          toaster.error("Perk Management", e.displayMessage)
          e.printStackTrace()
        }
    }
    outcome
  }

  private def popupNewOrderDialog(contestID: String, userID: String, aSymbol: js.UndefOr[String]): js.Promise[NewOrderDialogResult] = {
    val outcome = newOrderDialog.popup(new NewOrderParams(contestID = contestID, userID = userID, symbol = aSymbol))
    outcome onComplete {
      case Success(result) =>
        console.log(s"result = ${JSON.stringify(result)}")
        refreshView(contestID, userID)
      case Failure(e) =>
        if (e.getMessage != "cancel") {
          toaster.error("Order Management", e.displayMessage)
          e.printStackTrace()
        }
    }
    outcome
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
      rankings <- $scope.getPlayerRankings()
      joiningRank <- getJoiningPlayerRank(playerName, totalEquity, rankings)
    } yield joiningRank
  }

  $scope.getPlayerRankings = () => $scope.contest.flatMap(_.rankings)

  $scope.getRankCellClass = (aRanking: js.UndefOr[String]) => aRanking map {
    case rank if rank == "1st" & isTiedForLead => "rank_cell_2nd"
    case rank if Set("1st", "2nd", "3rd").contains(rank) => s"rank_cell_$rank"
    case rank if rank == "join" => "rank_cell_join"
    case _ => "rank_cell"
  }

  private def isTiedForLead: Boolean = {
    (for {
      rankings <- $scope.getPlayerRankings()
      _1st <- rankings.headOption.orUndefined
      _2nd <- rankings.drop(1).headOption.orUndefined
      _1stRank <- _1st.rankNum
      _2ndRank <- _2nd.rankNum
    } yield _1stRank == _2ndRank).toOption.contains(true)
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
    for {contestID <- aContestID; userID <- _userID} yield joinContest(contestID, userID)
  }

  $scope.quitContest = (aContestID: js.UndefOr[String]) => {
    for {contestID <- aContestID; userID <- _userID} yield quitContest(contestID, userID)
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
      case Success(_) =>
        $timeout(() => {
          $scope.isJoining = false
          $window.location.reload()
        }, 0.5.seconds)

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
    with AwardsSupportScope
    with ContestCssSupportScope
    with GlobalNavigation
    with GlobalSelectedSymbolScope
    with PlayerProfilePopupSupportScope
    with USMarketsStatusSupportScope {

    // functions
    var initDash: js.Function0[js.UndefOr[js.Promise[(Contest, Option[Portfolio])]]] = js.native
    var getJoiningPlayerRank: js.Function2[js.UndefOr[String], js.UndefOr[Double], js.UndefOr[String]] = js.native
    var getPlayerRankings: js.Function0[js.UndefOr[js.Array[ContestRanking]]] = js.native
    var getPortfolioTabs: js.Function0[js.Array[PortfolioTab]] = js.native
    var getRankCellClass: js.Function1[js.UndefOr[String], js.UndefOr[String]] = js.native
    var hasPerk: js.Function1[js.UndefOr[String], Boolean] = js.native
    var isParticipant: js.Function0[js.UndefOr[Boolean]] = js.native
    var isRankingsShown: js.Function0[Boolean] = js.native
    var playerRankingOnTop: js.Function2[js.UndefOr[String], js.UndefOr[js.Array[ContestRanking]], js.UndefOr[js.Array[ContestRanking]]] = js.native
    var toggleRankingsShown: js.Function0[Unit] = js.native

    // popup dialog functions
    var popupInvitePlayer: js.Function1[js.UndefOr[String], js.UndefOr[js.Promise[InvitePlayerDialogResult]]] = js.native
    var popupNewOrderDialog: js.Function3[js.UndefOr[String], js.UndefOr[String], js.UndefOr[String], js.UndefOr[js.Promise[NewOrderDialogResult]]] = js.native
    var popupPerksDialog: js.Function2[js.UndefOr[String], js.UndefOr[String], js.UndefOr[js.Promise[PerksDialogResult]]] = js.native

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
    var contest: js.UndefOr[Contest] = js.native
    var portfolio: js.UndefOr[Portfolio] = js.native
    var maxPlayers: js.UndefOr[Int] = js.native
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
  class PortfolioTab(val name: String,
                     val icon: String,
                     val path: String,
                     val isAuthRequired: Boolean = false,
                     var active: Boolean = false) extends js.Object

}