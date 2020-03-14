package com.shocktrade.client.contest

import com.shocktrade.client.ScopeEvents._
import com.shocktrade.client.dialogs.InvitePlayerDialog
import com.shocktrade.client.models.contest.ContestSearchResultUI
import com.shocktrade.common.forms.{ContestSearchForm, PlayerInfoForm}
import com.shocktrade.common.models.contest.ContestRanking
import com.shocktrade.common.models.contest.ContestSearchResult._
import com.shocktrade.common.models.user.User
import io.scalajs.JSON
import io.scalajs.dom.html.browser.console
import io.scalajs.npm.angularjs.AngularJsHelper._
import io.scalajs.npm.angularjs.toaster.Toaster
import io.scalajs.npm.angularjs.{Location, Timeout, angular, injected}
import io.scalajs.util.DurationHelper._
import io.scalajs.util.JsUnderOrHelper._
import io.scalajs.util.PromiseHelper.Implicits._

import scala.concurrent.duration._
import scala.language.postfixOps
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js
import scala.scalajs.js.JSConverters._
import scala.util.{Failure, Success}

/**
 * Game Search Controller
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
class GameSearchController($scope: GameSearchScope, $location: Location, $timeout: Timeout, toaster: Toaster,
                           @injected("ContestService") contestService: ContestService,
                           @injected("InvitePlayerDialog") invitePlayerDialog: InvitePlayerDialog,
                           @injected("PortfolioService") portfolioService: PortfolioService)
  extends GameController($scope, $location, toaster, portfolioService) {

  // public variables
  private var searchResults = js.Array[ContestSearchResultUI]()
  private var splitScreen: Boolean = false

  $scope.contest = null
  $scope.contestRankings = js.undefined
  $scope.portfolios = js.Array()
  $scope.searchTerm = null
  $scope.searchOptions = new ContestSearchForm(
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

  $scope.getSearchResults = (aSearchTerm: js.UndefOr[String]) => getSearchResults(aSearchTerm)

  $scope.getSelectedContest = () => $scope.selectedContest

  $scope.invitePlayerPopup = (aContest: js.UndefOr[ContestSearchResultUI], aPlayerID: js.UndefOr[String]) => invitePlayerPopup(aContest, aPlayerID)

  $scope.isParticipant = (aContest: js.UndefOr[ContestSearchResultUI]) => aContest exists isParticipant

  $scope.getAvailableCount = () => searchResults.count(_.isActive)

  $scope.getAvailableSlots = (aContest: js.UndefOr[ContestSearchResultUI], aRowIndex: js.UndefOr[Number]) => getAvailableSlots(aContest, aRowIndex)

  $scope.isActive = (aContest: js.UndefOr[ContestSearchResultUI]) => aContest.map(_.isActive)

  $scope.contestSearch = (aSearchOptions: js.UndefOr[ContestSearchForm]) => aSearchOptions foreach contestSearch

  private def contestSearch(searchOptions: ContestSearchForm): Unit = {
    asyncLoading($scope)(contestService.findContests(searchOptions)) onComplete {
      case Success(contests) =>
        $scope.$apply(() => searchResults = contests.data.map(_.asInstanceOf[ContestSearchResultUI]))
      case Failure(e) =>
        toaster.error("Failed to execute ContestSearchResultUI Search")
        console.error(s"Failed: searchOptions = ${angular.toJson(searchOptions)}")
    }
  }

  private def getAvailableSlots(aContest: js.UndefOr[ContestSearchResultUI], aRowIndex: js.UndefOr[Number]): js.UndefOr[js.Array[ContestRanking]] = aContest map { contest =>
    val row = aRowIndex.map(_.intValue()) getOrElse 0
    val start = row * 8
    val end = start + 8

    // generate the slots
    js.Array((start to end).map {
      case n if n < $scope.portfolios.length => $scope.portfolios(n)
      case _ => null
    }: _*)
  }

  private def invitePlayerPopup(aContest: js.UndefOr[ContestSearchResultUI], aUserID: js.UndefOr[String]): Unit = {
    for {
      contest <- aContest
      userID <- aUserID
    } {
      $scope.portfolio.toOption match {
        case Some(portfolio) =>
          invitePlayerDialog.popup(portfolio)
        case _ =>
          toaster.error("You must join the game to use this feature")
      }
    }
  }

  private def isParticipant(contest: ContestSearchResultUI): Boolean = {
    $scope.portfolios.exists(_.userID == $scope.userProfile.flatMap(_.userID))
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

  $scope.isSplitScreen = () => isSplitScreen

  $scope.selectContest = (aContest: js.UndefOr[ContestSearchResultUI]) => selectContest(aContest)

  $scope.toggleSplitScreen = () => toggleSplitScreen()

  private def isContestSelected(contestId: String) = $scope.selectedContest.exists(_.contestID.contains(contestId))

  private def isSplitScreen = splitScreen && $scope.selectedContest.exists(_.hostUserID.exists(_.nonEmpty))

  private def selectContest(aContest: js.UndefOr[ContestSearchResultUI]): Unit = {
    $scope.selectedContest = aContest
    $scope.portfolios.clear()

    for {contest <- aContest; contestID <- contest.contestID} {
      contestService.findRankingsByContest(contestID) onComplete {
        case Success(response) =>
          splitScreen = true
          $scope.$apply { () => $scope.portfolios = response.data }
          console.log(s"Selecting contest '${contest.name}' ($contestID)")
        case Failure(e) =>
          toaster.error(e.displayMessage)
      }
    }
  }

  private def toggleSplitScreen(): Unit = splitScreen = false

  ///////////////////////////////////////////////////////////////////////////
  //          ContestSearchResultUI Management Functions
  ///////////////////////////////////////////////////////////////////////////

  $scope.isDeletable = (aContest: js.UndefOr[ContestSearchResultUI]) => $scope.isContestOwner(aContest)

  $scope.isContestOwner = (aContest: js.UndefOr[ContestSearchResultUI]) => aContest exists { contest =>
    contest.hostUserID == $scope.userProfile.flatMap(_.userID)
  }

  $scope.deleteContest = (aContest: js.UndefOr[ContestSearchResultUI]) => deleteContest(aContest)

  $scope.isJoinable = (aContest: js.UndefOr[ContestSearchResultUI]) => aContest.flat exists { contest =>
    $scope.userProfile.isAssigned && !contest.invitationOnly.isTrue && !$scope.isParticipant(contest)
  }

  $scope.joinContest = (aContest: js.UndefOr[ContestSearchResultUI]) => joinContest(aContest)

  $scope.quitContest = (aContest: js.UndefOr[ContestSearchResultUI]) => quitContest(aContest)

  $scope.startContest = (aContest: js.UndefOr[ContestSearchResultUI]) => startContest(aContest)

  private def deleteContest(aContest: js.UndefOr[ContestSearchResultUI]): Unit = {
    for {
      contest <- aContest
      contestId <- contest.contestID
    } {
      contest.deleting = true
      console.log(s"Deleting contest ${contest.name}...")
      asyncLoading($scope)(contestService.deleteContest(contestId)) onComplete {
        case Success(response) =>
          console.log(s"response = ${JSON.stringify(response.data)}")
          $scope.$apply(() => removeContestFromList(searchResults, contestId))
          $timeout(() => contest.deleting = false, 0.5.seconds)
        case Failure(e) =>
          toaster.error("Error!", "Failed to delete contest")
          console.error("An error occurred while deleting the contest")
          $timeout(() => contest.deleting = false, 0.5.seconds)
      }
    }
  }

  private def joinContest(aContest: js.UndefOr[ContestSearchResultUI]): Unit = {
    for {
      contest <- aContest
      contestId <- contest.contestID
      userID <- $scope.userProfile.flatMap(_.userID)
      username <- $scope.userProfile.flatMap(_.username)
    } {
      contest.joining = true
      val form = new PlayerInfoForm(player = User(_id = userID, name = username))
      asyncLoading($scope)(contestService.joinContest(contestId, form)) onComplete {
        case Success(response) =>
          val joinedContest = response.data
          console.log(s"response = ${JSON.stringify(response.data)}")
          $scope.$apply { () =>
            //$scope.contest = joinedContest
            //mySession.setContest(joinedContest)
            //mySession.deduct(contest.startingBalance)
            //updateWithRankings(user.name, contest)
          }
          $timeout(() => contest.joining = false, 0.5.seconds)

        case Failure(e) =>
          toaster.error(title = "Error!", body = "Failed to join contest")
          console.error("An error occurred while joining the contest")
          $timeout(() => contest.joining = false, 0.5.seconds)
      }
    }
  }

  private def quitContest(aContest: js.UndefOr[ContestSearchResultUI]): Unit = {
    for {
      contest <- aContest
      userId <- $scope.userProfile.flatMap(_.userID)
      contestId <- contest.contestID
    } {
      contest.quitting = true
      asyncLoading($scope)(contestService.quitContest(contestId, userId)) onComplete {
        case Success(response) =>
          val updatedContest = response.data
          console.log(s"response = ${JSON.stringify(response.data)}")
          /*$scope.$apply { () =>
            $scope.contest = updatedContest
            mySession.setContest(updatedContest)
          }*/
          $timeout(() => contest.quitting = false, 0.5.seconds)

        case Failure(e) =>
          toaster.error(title = "Error!", e.displayMessage)
          console.error("An error occurred while joining the contest")
          $timeout(() => contest.quitting = false, 0.5.seconds)
      }
    }
  }

  private def startContest(aContest: js.UndefOr[ContestSearchResultUI]): Unit = {
    for {
      contest <- aContest
      contestId <- contest.contestID
    } {
      contest.starting = true
      asyncLoading($scope)(contestService.startContest(contestId)) onComplete {
        case Success(response) =>
          val theContest = response.data
          console.log(s"response = ${JSON.stringify(response.data)}")
          theContest.error foreach { error =>
            toaster.error(error)
            console.error(error)
          }
          $timeout(() => theContest.starting = false, 0.5.seconds)

        case Failure(e) =>
          toaster.error("An error occurred while starting the contest")
          console.error(s"Error starting contest: ${e.getMessage}")
          $timeout(() => contest.starting = false, 0.5.seconds)
      }
    }
  }

  //////////////////////////////////////////////////////////////////////
  //              Style/CSS Functions
  //////////////////////////////////////////////////////////////////////

  $scope.getSelectionClass = (aContest: js.UndefOr[ContestSearchResultUI]) => aContest map { c =>
    if ($scope.selectedContest.exists(_.contestID ?== c.contestID)) "selected"
    else if (c.isActive) ""
    else "null"
  }

  //////////////////////////////////////////////////////////////////////
  //              Broadcast Event Listeners
  //////////////////////////////////////////////////////////////////////

  private def updateContestInList(searchResults: js.Array[ContestSearchResultUI], contestId: String) {
    val index = searchResults.indexWhere(_.contestID.contains(contestId))
    if (index != -1) {
      asyncLoading($scope)(contestService.findContestByID(contestId)) onComplete {
        case Success(loadedContest) =>
        // $scope.$apply(() => searchResults(index) = loadedContest.data)
        case Failure(e) =>
          console.error(s"Error selecting feed: ${e.getMessage}")
          toaster.error("Error loading game")
      }
    }
  }

  private def removeContestFromList(searchResults: js.Array[_], contestId: String) {
    val index = indexOfContest(contestId)
    if (index != -1) {
      console.log(s"Removed contest $contestId from the list...")
      searchResults.splice(index, 1)
    }

    if ($scope.selectedContest.exists(_.contestID ?== contestId)) $scope.selectedContest = js.undefined
  }

  private def indexOfContest(contestId: String) = searchResults.indexWhere(_.contestID.contains(contestId))

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

  /**
   * Listen for contest update events
   */
  $scope.onContestUpdated { (_, contest) =>
    console.log(s"ContestSearchResultUI '${contest.name} updated")
    contest.contestID foreach { contestId =>
      // update the contest in our search results
      updateContestInList(searchResults, contestId)

      // make sure we"re pointing at the updated contest
      if (isContestSelected(contestId)) $scope.selectedContest = searchResults.find(_.contestID contains contestId).orUndefined
    }
  }

}

/**
 * Game Search Controller Scope
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
@js.native
trait GameSearchScope extends GameScope {
  // variables
  var contestRankings: js.UndefOr[js.Array[ContestRanking]] = js.native
  var searchTerm: String = js.native
  var searchOptions: ContestSearchForm = js.native
  var selectedContest: js.UndefOr[ContestSearchResultUI] = js.native
  var portfolios: js.Array[ContestRanking] = js.native

  // general functions
  var contestStatusClass: js.Function1[js.UndefOr[ContestSearchResultUI], js.UndefOr[String]] = js.native
  var getAvailableCount: js.Function0[Int] = js.native
  var getAvailableSlots: js.Function2[js.UndefOr[ContestSearchResultUI], js.UndefOr[Number], js.UndefOr[js.Array[ContestRanking]]] = js.native
  var getSelectionClass: js.Function1[js.UndefOr[ContestSearchResultUI], js.UndefOr[String]] = js.native
  var getStatusClass: js.Function1[js.UndefOr[ContestSearchResultUI], String] = js.native
  var isParticipant: js.Function1[js.UndefOr[ContestSearchResultUI], Boolean] = js.native
  var isSplitScreen: js.Function0[Boolean] = js.native
  var toggleSplitScreen: js.Function0[Unit] = js.native
  var trophy: js.Function1[js.UndefOr[String], js.UndefOr[String]] = js.native

  // contest functions
  var contestSearch: js.Function1[js.UndefOr[ContestSearchForm], Unit] = js.native
  var deleteContest: js.Function1[js.UndefOr[ContestSearchResultUI], Unit] = js.native
  var getSearchResults: js.Function1[js.UndefOr[String], js.Array[ContestSearchResultUI]] = js.native
  var getSelectedContest: js.Function0[js.UndefOr[ContestSearchResultUI]] = js.native
  var invitePlayerPopup: js.Function2[js.UndefOr[ContestSearchResultUI], js.UndefOr[String], Unit] = js.native
  var isActive: js.Function1[js.UndefOr[ContestSearchResultUI], js.UndefOr[Boolean]] = js.native
  var isContestOwner: js.Function1[js.UndefOr[ContestSearchResultUI], Boolean] = js.native
  var isDeletable: js.Function1[js.UndefOr[ContestSearchResultUI], Boolean] = js.native
  var isJoinable: js.Function1[js.UndefOr[ContestSearchResultUI], Boolean] = js.native
  var joinContest: js.Function1[js.UndefOr[ContestSearchResultUI], Unit] = js.native
  var quitContest: js.Function1[js.UndefOr[ContestSearchResultUI], Unit] = js.native
  var selectContest: js.Function1[js.UndefOr[ContestSearchResultUI], Unit] = js.native
  var startContest: js.Function1[js.UndefOr[ContestSearchResultUI], Unit] = js.native

}

