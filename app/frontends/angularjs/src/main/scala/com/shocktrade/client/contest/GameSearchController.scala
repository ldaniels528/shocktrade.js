package com.shocktrade.client.contest

import com.shocktrade.client.ScopeEvents._
import com.shocktrade.client.models.contest.ContestSearchResultUI
import com.shocktrade.client.users.GameStateFactory
import com.shocktrade.client.users.GameStateFactory.ContestScope
import com.shocktrade.client.{GlobalLoading, RootScope}
import com.shocktrade.common.forms.ContestSearchForm
import com.shocktrade.common.models.contest.ContestRanking
import com.shocktrade.common.models.contest.ContestSearchResult._
import io.scalajs.dom.html.browser.console
import io.scalajs.npm.angularjs.AngularJsHelper._
import io.scalajs.npm.angularjs._
import io.scalajs.npm.angularjs.toaster.Toaster
import io.scalajs.util.JsUnderOrHelper._
import io.scalajs.util.PromiseHelper.Implicits._

import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js
import scala.util.{Failure, Success}

/**
 * Game Search Controller
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
case class GameSearchController($scope: GameSearchScope, $location: Location, $timeout: Timeout, toaster: Toaster,
                                @injected("ContestService") contestService: ContestService,
                                @injected("GameStateFactory") gameState: GameStateFactory,
                                @injected("PortfolioService") portfolioService: PortfolioService)
  extends Controller with ContestEntrySupport[GameSearchScope] with GlobalLoading {

  // internal variables
  implicit private val scope: GameSearchScope = $scope
  private var searchResults = js.Array[ContestSearchResultUI]()

  $scope.contest = js.undefined
  $scope.rankings = js.undefined
  $scope.portfolios = js.Array()
  $scope.searchTerm = js.undefined
  $scope.searchOptions = new ContestSearchForm(
    userID = gameState.userID,
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
  //          Public Functions
  ///////////////////////////////////////////////////////////////////////////

  $scope.getAvailableCount = () => searchResults.count(_.isActive)

  $scope.getSearchResults = (aSearchTerm: js.UndefOr[String]) => getSearchResults(aSearchTerm)

  $scope.getSelectedContest = () => $scope.selectedContest

  $scope.contestSearch = (aSearchOptions: js.UndefOr[ContestSearchForm]) => aSearchOptions foreach contestSearch

  private def contestSearch(searchOptions: ContestSearchForm): Unit = {
    searchOptions.userID = gameState.userID
    asyncLoading($scope)(contestService.findContests(searchOptions)) onComplete {
      case Success(contests) =>
        $scope.$apply(() => searchResults = contests.data.map(_.asInstanceOf[ContestSearchResultUI]))
      case Failure(e) =>
        toaster.error("Failed to execute Contest Search")
        console.error(s"Failed: searchOptions = ${angular.toJson(searchOptions)}")
    }
  }

  private def getSearchResults(aSearchTerm: js.UndefOr[String]): js.Array[ContestSearchResultUI] = aSearchTerm.toOption match {
    case Some(searchTerm) =>
      val term = searchTerm.trim.toLowerCase
      searchResults.filter(_.name.exists(_.toLowerCase.contains(term)))
    case None => searchResults
  }

  $scope.contestStatusClass = (aContest: js.UndefOr[ContestSearchResultUI]) => aContest map {
    case contest if contest.isActive => "positive"
    case contest if contest.isClosed => "negative"
    case _ => ""
  }

  $scope.getStatusClass = (aContest: js.UndefOr[ContestSearchResultUI]) => getStatusClass(aContest)

  $scope.trophy = (aPlace: js.UndefOr[String]) => aPlace map trophy

  private def getStatusClass(aContest: js.UndefOr[ContestSearchResultUI]): String = {
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

  $scope.selectContest = (aContest: js.UndefOr[ContestSearchResultUI]) => selectContest(aContest)

  private def selectContest(aContest: js.UndefOr[ContestSearchResultUI]): Unit = {
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

  $scope.isActive = (aContest: js.UndefOr[ContestSearchResultUI]) => aContest.map(_.isActive)

  //////////////////////////////////////////////////////////////////////
  //              Style/CSS Functions
  //////////////////////////////////////////////////////////////////////

  $scope.getSelectionClass = (aContest: js.UndefOr[ContestSearchResultUI]) => aContest map { c =>
    if ($scope.selectedContest.exists(_.contestID ?== c.contestID)) "selected"
    else if (c.isActive) ""
    else "null"
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
    $scope.selectedContest = js.undefined
    searchResults = searchResults.filterNot(_.contestID ?== contest.contestID)
  }

}

/**
 * Game Search Controller Scope
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
@js.native
trait GameSearchScope extends RootScope with ContestScope with ContestEntrySupportScope {
  // variables
  //var contest: js.UndefOr[Contest] = js.native
  var rankings: js.UndefOr[js.Array[ContestRanking]] = js.native
  var searchTerm: js.UndefOr[String] = js.native
  var searchOptions: ContestSearchForm = js.native
  var selectedContest: js.UndefOr[ContestSearchResultUI] = js.native
  var portfolios: js.Array[ContestRanking] = js.native

  // general functions
  var contestStatusClass: js.Function1[js.UndefOr[ContestSearchResultUI], js.UndefOr[String]] = js.native
  var getAvailableCount: js.Function0[Int] = js.native
  var getSelectionClass: js.Function1[js.UndefOr[ContestSearchResultUI], js.UndefOr[String]] = js.native
  var getStatusClass: js.Function1[js.UndefOr[ContestSearchResultUI], String] = js.native
  var trophy: js.Function1[js.UndefOr[String], js.UndefOr[String]] = js.native

  // contest functions
  var contestSearch: js.Function1[js.UndefOr[ContestSearchForm], Unit] = js.native
  var getSearchResults: js.Function1[js.UndefOr[String], js.Array[ContestSearchResultUI]] = js.native
  var getSelectedContest: js.Function0[js.UndefOr[ContestSearchResultUI]] = js.native
  var isActive: js.Function1[js.UndefOr[ContestSearchResultUI], js.UndefOr[Boolean]] = js.native
  var selectContest: js.Function1[js.UndefOr[ContestSearchResultUI], Unit] = js.native

}

