package com.shocktrade.client.contest

import com.shocktrade.client.ScopeEvents._
import com.shocktrade.client.dialogs.InvitePlayerDialog
import com.shocktrade.client.models.UserProfile
import com.shocktrade.client.models.contest.{ContestSearchResultUI, Portfolio}
import com.shocktrade.client.{GlobalLoading, MySessionService}
import com.shocktrade.common.forms.{ContestSearchForm, PlayerInfoForm}
import com.shocktrade.common.models.contest.ContestRanking
import com.shocktrade.common.models.contest.ContestSearchResult._
import com.shocktrade.common.models.user.User
import io.scalajs.JSON
import io.scalajs.dom.html.browser.console
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
                           @injected("MySessionService") mySession: MySessionService,
                           @injected("PortfolioService") portfolioService: PortfolioService)
  extends GameController($scope, $location, toaster, mySession, portfolioService) with GlobalLoading {

  // public variables
  private var searchResults = js.Array[ContestSearchResultUI]()
  private var selectedContest: js.UndefOr[ContestSearchResultUI] = js.undefined
  private var portfolios = js.Array[Portfolio]()
  private var splitScreen: Boolean = false

  $scope.contest = null
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

  $scope.getSelectedContest = () => selectedContest

  $scope.invitePlayerPopup = (aContest: js.UndefOr[ContestSearchResultUI], aPlayerID: js.UndefOr[String]) => {
    for {
      contest <- aContest
      portfolioID <- aPlayerID
    } {
      mySession.participant_? match {
        case Some(participant) =>
          invitePlayerDialog.popup(participant)
        case _ =>
          toaster.error("You must join the game to use this feature")
      }
    }
  }

  $scope.getAvailableCount = () => searchResults.count(_.isActive)

  $scope.getAvailableSlots = (aContest: js.UndefOr[ContestSearchResultUI], aRowIndex: js.UndefOr[Number]) => aContest map { contest =>
    val row = aRowIndex.map(_.intValue()) getOrElse 0
    val start = row * 8
    val end = start + 8

    // generate the slots
    js.Array((start to end) map {
      case n if n < portfolios.length => portfolios(n)
      case _ => null
    }: _*)
  }

  $scope.isActive = (aContest: js.UndefOr[ContestSearchResultUI]) => aContest.map(_.isActive)

  $scope.contestSearch = (aSearchOptions: js.UndefOr[ContestSearchForm]) => aSearchOptions foreach { searchOptions =>
    asyncLoading($scope)(contestService.findContests(searchOptions)) onComplete {
      case Success(contests) =>
        $scope.$apply(() => searchResults = contests.data.map(_.asInstanceOf[ContestSearchResultUI]))
      case Failure(e) =>
        toaster.error("Failed to execute ContestSearchResultUI Search")
        console.error(s"Failed: searchOptions = ${angular.toJson(searchOptions)}")
    }
  }

  $scope.getSearchResults = (aSearchTerm: js.UndefOr[String]) => {
    aSearchTerm.toOption match {
      case Some(searchTerm) =>
        val term = searchTerm.trim.toLowerCase
        searchResults.filter(_.name.exists(_.toLowerCase.contains(term)))
      case None => searchResults
    }
  }

  $scope.contestStatusClass = (aContest: js.UndefOr[ContestSearchResultUI]) => aContest map {
    case contest if contest.isActive => "positive"
    case contest if contest.isClosed => "negative"
    case _ => ""
  }

  $scope.getStatusClass = (aContest: js.UndefOr[ContestSearchResultUI]) => {
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

  $scope.trophy = (aPlace: js.UndefOr[String]) => aPlace map {
    case "1st" => "contests/gold.png"
    case "2nd" => "contests/silver.png"
    case "3rd" => "contests/bronze.png"
    case _ => "status/transparent.png"
  }

  ///////////////////////////////////////////////////////////////////////////
  //          ContestSearchResultUI Selection Functions
  ///////////////////////////////////////////////////////////////////////////

  $scope.isSplitScreen = () => splitScreen && selectedContest.isDefined

  $scope.toggleSplitScreen = () => splitScreen = false

  $scope.selectContest = (aContest: js.UndefOr[ContestSearchResultUI]) => {
    selectedContest = aContest
    for {
      contest <- aContest
      contestId <- contest.contestID
    } {
      console.log(s"Selecting contest '${contest.name}' ($contestId)")
      splitScreen = true
      //mySession.updateRankings(contest)
    }
  }

  private def isContestSelected(contestId: String) = selectedContest.exists(_.contestID.contains(contestId))

  ///////////////////////////////////////////////////////////////////////////
  //          ContestSearchResultUI Management Functions
  ///////////////////////////////////////////////////////////////////////////

  $scope.containsPlayer = (aContest: js.UndefOr[ContestSearchResultUI], aUserProfile: js.UndefOr[UserProfile]) => {
    aContest.flatMap(_.hostUserID) == aUserProfile.flatMap(_.userID) // TODO fix me
  }

  $scope.isDeletable = (aContest: js.UndefOr[ContestSearchResultUI]) => $scope.isContestOwner(aContest)

  $scope.isContestOwner = (aContest: js.UndefOr[ContestSearchResultUI]) => aContest exists { contest =>
    contest.hostUserID == mySession.userProfile.userID
  }

  $scope.deleteContest = (aContest: js.UndefOr[ContestSearchResultUI]) => {
    for {
      contest <- aContest
      contestId <- contest.contestID.toOption
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

  $scope.isJoinable = (aContest: js.UndefOr[ContestSearchResultUI]) => aContest.flat exists { contest =>
    mySession.isAuthenticated &&
      !contest.invitationOnly.isTrue &&
      contest.hostUserID != mySession.userProfile.userID &&
      !$scope.isParticipant(contest)
  }

  $scope.joinContest = (aContest: js.UndefOr[ContestSearchResultUI]) => {
    for {
      contest <- aContest
      contestId <- contest.contestID.toOption
      userId <- mySession.userProfile.userID.toOption
    } {
      contest.joining = true
      val form = new PlayerInfoForm(player = User(_id = userId, name = mySession.userProfile.username))
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
          toaster.error("Error!", "Failed to join contest")
          console.error("An error occurred while joining the contest")
          $timeout(() => contest.joining = false, 0.5.seconds)
      }
    }
  }

  $scope.quitContest = (aContest: js.UndefOr[ContestSearchResultUI]) => {
    for {
      contest <- aContest
      userId <- mySession.userProfile.userID.toOption
      contestId <- contest.contestID.toOption
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
          console.error("An error occurred while joining the contest")
          $timeout(() => contest.quitting = false, 0.5.seconds)
      }
    }
  }

  $scope.startContest = (aContest: js.UndefOr[ContestSearchResultUI]) => {
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
    if (selectedContest.exists(_.contestID ?== c.contestID)) "selected"
    else if (c.isActive) ""
    else "null"
  }

  //////////////////////////////////////////////////////////////////////
  //              Broadcast Event Listeners
  //////////////////////////////////////////////////////////////////////

  private def indexOfContest(contestId: String) = searchResults.indexWhere(_.contestID.contains(contestId))

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

    if (selectedContest.exists(_.contestID ?== contestId)) selectedContest = js.undefined
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
    selectedContest = js.undefined
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
      if (isContestSelected(contestId)) selectedContest = searchResults.find(_.contestID contains contestId).orUndefined
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
  var contest: ContestSearchResultUI = js.native
  var searchTerm: String = js.native
  var searchOptions: ContestSearchForm = js.native
  var portfolios: js.Array[ContestRanking] = js.native

  // general functions
  var contestStatusClass: js.Function1[js.UndefOr[ContestSearchResultUI], js.UndefOr[String]] = js.native
  var getAvailableCount: js.Function0[Int] = js.native
  var getAvailableSlots: js.Function2[js.UndefOr[ContestSearchResultUI], js.UndefOr[Number], js.UndefOr[js.Array[Portfolio]]] = js.native
  var getSelectionClass: js.Function1[js.UndefOr[ContestSearchResultUI], js.UndefOr[String]] = js.native
  var getStatusClass: js.Function1[js.UndefOr[ContestSearchResultUI], String] = js.native
  var isSplitScreen: js.Function0[Boolean] = js.native
  var toggleSplitScreen: js.Function0[Unit] = js.native
  var trophy: js.Function1[js.UndefOr[String], js.UndefOr[String]] = js.native

  // contest functions
  var containsPlayer: js.Function2[js.UndefOr[ContestSearchResultUI], js.UndefOr[UserProfile], Boolean] = js.native
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

