package com.shocktrade.client.contest

import com.shocktrade.client.GameState._
import com.shocktrade.client.ScopeEvents._
import com.shocktrade.client.contest.GameSearchController._
import com.shocktrade.client.dialogs.{NewGameDialog, NewGameDialogController}
import com.shocktrade.client.models.UserProfile
import com.shocktrade.client.models.contest.Contest
import com.shocktrade.client.users.UserService
import com.shocktrade.client.{GlobalLoading, RootScope}
import com.shocktrade.common.AppConstants
import com.shocktrade.common.forms.ContestCreationForm.{GameBalance, GameDuration}
import com.shocktrade.common.forms.ContestSearchForm
import com.shocktrade.common.forms.ContestSearchForm.ContestStatus
import com.shocktrade.common.models.contest.ContestSearchResult._
import com.shocktrade.common.models.contest.{ContestRanking, ContestSearchResult, MyContest}
import io.scalajs.JSON
import io.scalajs.dom.html.browser.console
import io.scalajs.npm.angularjs.AngularJsHelper._
import io.scalajs.npm.angularjs._
import io.scalajs.npm.angularjs.cookies.Cookies
import io.scalajs.npm.angularjs.http.HttpResponse
import io.scalajs.npm.angularjs.toaster.Toaster
import io.scalajs.util.JsUnderOrHelper._
import io.scalajs.util.PromiseHelper.Implicits._

import scala.concurrent.Future
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js
import scala.scalajs.js.JSConverters._
import scala.util.{Failure, Success}

/**
 * Game Search Controller
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
case class GameSearchController($scope: GameSearchScope, $cookies: Cookies, $location: Location, $timeout: Timeout, toaster: Toaster,
                                @injected("ContestService") contestService: ContestService,
                                @injected("NewGameDialog") newGameDialog: NewGameDialog,
                                @injected("PortfolioService") portfolioService: PortfolioService,
                                @injected("UserService") userService: UserService)
  extends Controller with ContestCssSupport with ContestEntrySupport with GlobalLoading {

  // internal variables
  implicit private val cookies: Cookies = $cookies
  private var searchResults = js.Array[ContestSearchResult]()
  private var myContests = js.Array[MyContest]()

  $scope.maxPlayers = AppConstants.MaxPlayers
  $scope.contest = js.undefined
  $scope.rankings = js.undefined
  $scope.portfolios = js.Array()

  $scope.buyIns = NewGameDialogController.StartingBalances
  $scope.durations = NewGameDialogController.GameDurations
  $scope.statuses = js.Array(
    new ContestStatus(statusID = 1, description = "Active and Queued"),
    new ContestStatus(statusID = 2, description = "Active Only"),
    new ContestStatus(statusID = 3, description = "Queued Only"),
    new ContestStatus(statusID = 4, description = "All")
  )

  $scope.searchOptions = new ContestSearchForm(
    userID = $cookies.getGameState.userID,
    buyIn = js.undefined,
    continuousTrading = false,
    duration = js.undefined,
    friendsOnly = false,
    invitationOnly = false,
    levelCap = js.undefined,
    levelCapAllowed = false,
    nameLike = js.undefined,
    perksAllowed = false,
    robotsAllowed = false,
    status = $scope.statuses.headOption.orUndefined
  )

  ///////////////////////////////////////////////////////////////////////////
  //          Initialization Functions
  ///////////////////////////////////////////////////////////////////////////

  $scope.initMyGames = () => initMyGames()

  /**
   * Retrieves the collection of games for the authenticated user
   */
  private def initMyGames(): js.UndefOr[js.Promise[HttpResponse[js.Array[MyContest]]]] = $scope.userProfile.flatMap(_.userID) map loadMyContests

  /**
   * Listen for contest creation events
   */
  $scope.onContestCreated((_, _) => initMyGames())

  /**
   * Listen for contest deletion events
   */
  $scope.onContestDeleted((_, _) => initMyGames())

  /**
   * Listen for contest selected events
   */
  $scope.onContestSelected((_, _) => initMyGames())

  /**
   * Listen for user profile changes
   */
  $scope.onUserProfileUpdated((_, _) => initMyGames())

  ///////////////////////////////////////////////////////////////////////////
  //          Public Functions
  ///////////////////////////////////////////////////////////////////////////

  $scope.getAvailableCount = () => searchResults.count(_.isActive)

  $scope.getSearchResults = (aSearchTerm: js.UndefOr[String]) => getSearchResults(aSearchTerm)

  $scope.contestSearch = (aSearchOptions: js.UndefOr[ContestSearchForm]) => aSearchOptions map contestSearch

  $scope.isActive = (aContest: js.UndefOr[ContestSearchResult]) => aContest.map(_.isActive)

  private def contestSearch(searchOptions: ContestSearchForm): js.Promise[HttpResponse[js.Array[ContestSearchResult]]] = {
    searchOptions.userID = $cookies.getGameState.userID
    val outcome = contestService.findContests(searchOptions)
    asyncLoading($scope)(outcome) onComplete {
      case Success(contests) =>
        $scope.$apply(() => searchResults = contests.data)
      case Failure(e) =>
        toaster.error("Failed to execute Contest Search")
        console.error(s"Failed: searchOptions = ${angular.toJson(searchOptions)}")
    }
    outcome
  }

  private def getSearchResults(aSearchTerm: js.UndefOr[String]): js.Array[ContestSearchResult] = aSearchTerm.toOption match {
    case Some(searchTerm) =>
      val term = searchTerm.trim.toLowerCase
      searchResults.filter(_.name.exists(_.toLowerCase.contains(term)))
    case None => searchResults
  }

  ///////////////////////////////////////////////////////////////////////////
  //          Contest Expansion Functions
  ///////////////////////////////////////////////////////////////////////////

  $scope.expandContest = (aContest: js.UndefOr[ContestSearchResult]) => {
    for {
      contest <- aContest
      contestID <- contest.contestID
    } yield expandContest(contest, contestID)
  }

  def expandContest(contest: ContestSearchResult, contestID: String): js.Promise[js.Array[ContestRanking]] = {
    contest.isExpanded = !contest.isExpanded.isTrue
    contest.rankings = js.undefined

    if (contest.isExpanded.isTrue) {
      contest.isLoading = true
      val outcome = contestService.findRankingsByContest(contestID).map(_.data)
      outcome onComplete {
        case Success(rankings) =>
          $scope.$apply { () =>
            contest.isLoading = false
            contest.rankings = rankings.sortBy(_.rankNum.getOrElse(Int.MaxValue))
          }
        case Failure(e) =>
          $scope.$apply(() => contest.isLoading = false)
          console.error(s"Failed to expand contest: ${e.getMessage}")
      }
      outcome.toJSPromise
    }
    else Future.successful(js.Array[ContestRanking]()).toJSPromise
  }

  ///////////////////////////////////////////////////////////////////////////
  //          Event Listeners
  ///////////////////////////////////////////////////////////////////////////

  /**
   * Listen for contest creation events
   */
  $scope.onContestCreated { (_, contest) =>
    console.log(s"New contest created '${contest.name}'")
    //searchResults.push(contest)
    //mySession.refresh()
  }

  /**
   * Listen for contest deletion events
   */
  $scope.onContestDeleted { (_, contest) =>
    searchResults = searchResults.filterNot(_.contestID ?== contest.contestID)
  }

  ///////////////////////////////////////////////////////////////////////////
  //          My Games Methods
  ///////////////////////////////////////////////////////////////////////////

  $scope.getMyContests = () => myContests

  $scope.popupNewGameDialog = (aUserID: js.UndefOr[String]) => aUserID.map(popupNewGameDialog)

  private def popupNewGameDialog(userID: String): js.Promise[HttpResponse[UserProfile]] = {
    val outcome = for {
      _ <- newGameDialog.popup(userID)
      userProfile <- userService.findUserByID(userID)
    } yield userProfile

    outcome onComplete {
      case Success(userProfile) =>
        initMyGames()
        $scope.emitUserProfileUpdated(userProfile.data)
      case Failure(e) =>
        toaster.error("Failed to create game")
        console.error(s"Failed to create game: ${e.displayMessage}")
    }
    outcome.toJSPromise
  }

  private def loadMyContests(userID: String): js.Promise[HttpResponse[js.Array[MyContest]]] = {
    console.log(s"Loading 'My Contests' for user '$userID'...")
    val outcome = contestService.findMyContests(userID)
    outcome onComplete {
      case Success(response) =>
        val contests = response.data
        console.log(s"Loaded ${contests.length} contest(s)")
        $scope.$apply(() => myContests = contests)
      case Failure(e) =>
        toaster.error("Failed to load 'My Contests'")
        console.error(s"Failed to load 'My Contests': ${JSON.stringify(e.displayMessage)}")
    }
    outcome
  }

}

/**
 * Game Search Controller Companion
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
object GameSearchController {

  /**
   * Game Search Controller Scope
   * @author Lawrence Daniels <lawrence.daniels@gmail.com>
   */
  @js.native
  trait GameSearchScope extends RootScope with ContestCssSupportScope with ContestEntrySupportScope {
    // variables
    var buyIns: js.Array[GameBalance] = js.native
    var contest: js.UndefOr[Contest] = js.native
    var durations: js.Array[GameDuration] = js.native
    var maxPlayers: js.UndefOr[Int] = js.native
    var rankings: js.UndefOr[js.Array[ContestRanking]] = js.native
    var searchOptions: ContestSearchForm = js.native
    var portfolios: js.Array[ContestRanking] = js.native
    var statuses: js.Array[ContestStatus] = js.native

    // contest search functions
    var contestSearch: js.Function1[js.UndefOr[ContestSearchForm], js.UndefOr[js.Promise[HttpResponse[js.Array[ContestSearchResult]]]]] = js.native
    var expandContest: js.Function1[js.UndefOr[ContestSearchResult], js.UndefOr[js.Promise[js.Array[ContestRanking]]]] = js.native
    var getAvailableCount: js.Function0[Int] = js.native
    var getSearchResults: js.Function1[js.UndefOr[String], js.Array[ContestSearchResult]] = js.native
    var isActive: js.Function1[js.UndefOr[ContestSearchResult], js.UndefOr[Boolean]] = js.native

    // my games functions
    var initMyGames: js.Function0[js.UndefOr[js.Promise[HttpResponse[js.Array[MyContest]]]]] = js.native
    var getMyContests: js.Function0[js.Array[MyContest]] = js.native
    var popupNewGameDialog: js.Function1[js.UndefOr[String], js.UndefOr[js.Promise[HttpResponse[UserProfile]]]] = js.native

  }

}
