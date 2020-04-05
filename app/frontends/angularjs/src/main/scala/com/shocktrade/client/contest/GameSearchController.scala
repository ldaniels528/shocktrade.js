package com.shocktrade.client.contest

import com.shocktrade.client.GameState._
import com.shocktrade.client.ScopeEvents._
import com.shocktrade.client.dialogs.NewGameDialog
import com.shocktrade.client.models.contest.Contest
import com.shocktrade.client.users.UserService
import com.shocktrade.client.{GlobalLoading, RootScope}
import com.shocktrade.common.AppConstants
import com.shocktrade.common.forms.ContestSearchForm
import com.shocktrade.common.models.contest.ContestSearchResult._
import com.shocktrade.common.models.contest.{ContestRanking, ContestSearchResult, MyContest}
import io.scalajs.JSON
import io.scalajs.dom.html.browser.console
import io.scalajs.npm.angularjs.AngularJsHelper._
import io.scalajs.npm.angularjs._
import io.scalajs.npm.angularjs.cookies.Cookies
import io.scalajs.npm.angularjs.toaster.Toaster
import io.scalajs.util.JsUnderOrHelper._
import io.scalajs.util.PromiseHelper.Implicits._

import scala.concurrent.Future
import scala.language.postfixOps
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
  extends Controller with ContestEntrySupport[GameSearchScope] with GlobalLoading {

  // internal variables
  implicit private val cookies: Cookies = $cookies
  private var searchResults = js.Array[ContestSearchResult]()
  private var myContests = js.Array[MyContest]()

  $scope.maxPlayers = AppConstants.MaxPlayers
  $scope.contest = js.undefined
  $scope.rankings = js.undefined
  $scope.portfolios = js.Array()
  $scope.searchTerm = js.undefined

  $scope.searchOptions = new ContestSearchForm(
    userID = $cookies.getGameState.userID,
    activeOnly = false,
    available = false,
    friendsOnly = false,
    invitationOnly = false,
    levelCap = js.undefined,
    levelCapAllowed = false,
    perksAllowed = false,
    robotsAllowed = false
  )

  ///////////////////////////////////////////////////////////////////////////
  //          Initialization Functions
  ///////////////////////////////////////////////////////////////////////////

  $scope.initMyGames = () => initMyGames()

  /**
   * Retrieves the collection of games for the authenticated user
   */
  private def initMyGames(): Unit = $scope.userProfile.flatMap(_.userID) foreach loadMyContests

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

  $scope.getSelectedContest = () => $scope.selectedContest

  $scope.contestSearch = (aSearchOptions: js.UndefOr[ContestSearchForm]) => aSearchOptions foreach contestSearch

  private def contestSearch(searchOptions: ContestSearchForm): Unit = {
    searchOptions.userID = $cookies.getGameState.userID
    asyncLoading($scope)(contestService.findContests(searchOptions)) onComplete {
      case Success(contests) =>
        $scope.$apply(() => searchResults = contests.data.map(_.asInstanceOf[ContestSearchResult]))
      case Failure(e) =>
        toaster.error("Failed to execute Contest Search")
        console.error(s"Failed: searchOptions = ${angular.toJson(searchOptions)}")
    }
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
    } yield {
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
  }

  ///////////////////////////////////////////////////////////////////////////
  //          Contest CSS Functions
  ///////////////////////////////////////////////////////////////////////////

  $scope.contestStatusClass = (aContest: js.UndefOr[ContestSearchResult]) => aContest map {
    case contest if contest.isActive => "positive"
    case contest if contest.isClosed => "negative"
    case _ => ""
  }

  $scope.getExpandedTrophyIcon = (aRanking: js.UndefOr[ContestRanking]) => aRanking.flatMap(_.rankNum).map {
    case 1 => "fa fa-trophy ds-1st"
    case 2 => "fa fa-trophy ds-2nd"
    case 3 => "fa fa-trophy ds-3rd"
    case _ => "fa fa-trophy ds-nth"
  }

  $scope.getSelectionClass = (aContest: js.UndefOr[ContestSearchResult]) => aContest map { c =>
    if ($scope.selectedContest.exists(_.contestID ?== c.contestID)) "selected"
    else if (c.isActive) ""
    else "null"
  }

  $scope.getStatusClass = (aContest: js.UndefOr[ContestSearchResult]) => getStatusClass(aContest)

  $scope.trophy = (aPlace: js.UndefOr[String]) => aPlace map trophy

  private def getStatusClass(aContest: js.UndefOr[ContestSearchResult]): String = {
    aContest map {
      case c if c.isEmpty => ""
      case c if c.isFull => "negative"
      case c if !c.isAlmostFull => "positive"
      case c if c.isAlmostFull => "warning"
      case c if c.isActive => "positive"
      case c if c.isClosed => "negative"
      case _ => "null"
    } getOrElse ""
  }

  private def trophy(place: String): String = place match {
    case "1st" => "contests/gold.png"
    case "2nd" => "contests/silver.png"
    case "3rd" => "contests/bronze.png"
    case _ => "status/transparent.png"
  }

  ///////////////////////////////////////////////////////////////////////////
  //          Contest Selection Functions
  ///////////////////////////////////////////////////////////////////////////

  $scope.selectContest = (aContest: js.UndefOr[ContestSearchResult]) => selectContest(aContest)

  private def selectContest(aContest: js.UndefOr[ContestSearchResult]): Unit = {
    $scope.selectedContest = aContest
    $scope.portfolios.clear()

    for {contest <- aContest; contestID <- contest.contestID} {
      contestService.findRankingsByContest(contestID) onComplete {
        case Success(response) =>
          $scope.$apply { () => $scope.portfolios = response.data }
          console.log(s"Selecting contest '${contest.name}' ($contestID)")
        case Failure(e) =>
          toaster.error(e.displayMessage)
      }
    }
  }

  ///////////////////////////////////////////////////////////////////////////
  //          Contest Management Functions
  ///////////////////////////////////////////////////////////////////////////

  $scope.isActive = (aContest: js.UndefOr[ContestSearchResult]) => aContest.map(_.isActive)

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
    $scope.selectedContest = js.undefined
    searchResults = searchResults.filterNot(_.contestID ?== contest.contestID)
  }

  ///////////////////////////////////////////////////////////////////////////
  //          My Games Methods
  ///////////////////////////////////////////////////////////////////////////

  $scope.getMyContests = () => myContests

  $scope.popupNewGameDialog = (aUserID: js.UndefOr[String]) => aUserID.foreach(popupNewGameDialog)

  private def popupNewGameDialog(userID: String): Unit = {
    val outcome = for {
      response <- newGameDialog.popup(userID)
      userProfile <- userService.findUserByID(userID)
    } yield (response, userProfile)

    outcome onComplete {
      case Success((_, userProfile)) =>
        initMyGames()
        $scope.emitUserProfileUpdated(userProfile.data)
      case Failure(e) =>
        toaster.error("Failed to create game")
        console.error(s"Failed to create game: ${e.displayMessage}")
    }
  }

  private def loadMyContests(userID: String): Unit = {
    console.log(s"Loading 'My Contests' for user '$userID'...")
    contestService.findMyContests(userID) onComplete {
      case Success(response) =>
        val contests = response.data
        console.log(s"Loaded ${contests.length} contest(s)")
        $scope.$apply(() => myContests = contests)
      case Failure(e) =>
        toaster.error("Failed to load 'My Contests'")
        console.error(s"Failed to load 'My Contests': ${JSON.stringify(e.displayMessage)}")
    }
  }

}

/**
 * Game Search Controller Scope
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
@js.native
trait GameSearchScope extends RootScope with ContestEntrySupportScope {
  // variables
  var contest: js.UndefOr[Contest] = js.native
  var maxPlayers: js.UndefOr[Int] = js.native
  var rankings: js.UndefOr[js.Array[ContestRanking]] = js.native
  var searchTerm: js.UndefOr[String] = js.native
  var searchOptions: ContestSearchForm = js.native
  var selectedContest: js.UndefOr[ContestSearchResult] = js.native
  var portfolios: js.Array[ContestRanking] = js.native

  // CSS class functions
  var contestStatusClass: js.Function1[js.UndefOr[ContestSearchResult], js.UndefOr[String]] = js.native
  var getSelectionClass: js.Function1[js.UndefOr[ContestSearchResult], js.UndefOr[String]] = js.native
  var getStatusClass: js.Function1[js.UndefOr[ContestSearchResult], String] = js.native
  var trophy: js.Function1[js.UndefOr[String], js.UndefOr[String]] = js.native

  // contest search functions
  var contestSearch: js.Function1[js.UndefOr[ContestSearchForm], Unit] = js.native
  var expandContest: js.Function1[js.UndefOr[ContestSearchResult], js.UndefOr[js.Promise[js.Array[ContestRanking]]]] = js.native
  var getAvailableCount: js.Function0[Int] = js.native
  var getExpandedTrophyIcon: js.Function1[js.UndefOr[ContestRanking], js.UndefOr[String]] = js.native
  var getSearchResults: js.Function1[js.UndefOr[String], js.Array[ContestSearchResult]] = js.native
  var getSelectedContest: js.Function0[js.UndefOr[ContestSearchResult]] = js.native
  var isActive: js.Function1[js.UndefOr[ContestSearchResult], js.UndefOr[Boolean]] = js.native
  var selectContest: js.Function1[js.UndefOr[ContestSearchResult], Unit] = js.native

  // my games functions
  var initMyGames: js.Function0[Unit] = js.native
  var getMyContests: js.Function0[js.Array[MyContest]] = js.native
  var popupNewGameDialog: js.Function1[js.UndefOr[String], Unit] = js.native

}
