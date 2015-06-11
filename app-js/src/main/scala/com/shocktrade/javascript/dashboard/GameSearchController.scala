package com.shocktrade.javascript.dashboard

import biz.enef.angulate.core.{HttpError, Location, Timeout}
import biz.enef.angulate.{Scope, ScopeController, named}
import com.ldaniels528.angularjs.Toaster
import com.shocktrade.javascript.MySession
import com.shocktrade.javascript.ScalaJsHelper._

import scala.language.postfixOps
import scala.scalajs.js
import scala.scalajs.js.Dynamic.{global => g, literal => JS}
import scala.scalajs.js.JSON

/**
 * Game Search Controller
 * @author lawrence.daniels@gmail.com
 */
class GameSearchController($scope: js.Dynamic, $location: Location, $routeParams: js.Dynamic, $timeout: Timeout, toaster: Toaster,
                           @named("ContestService") contestService: ContestService,
                           @named("InvitePlayerDialog") invitePlayerDialog: js.Dynamic,
                           @named("MySession") mySession: MySession) extends ScopeController {
  val scope = $scope.asInstanceOf[Scope]
  val MaxPlayers = 24 // TODO

  // public variables
  var searchResults = js.Array[js.Dynamic]()
  var selectedContest: js.Dynamic = null
  var splitScreen = false

  // search variables
  var searchOptions = JS(
    activeOnly = false,
    available = false,
    friendsOnly = false,
    levelCap = "1",
    levelCapAllowed = false,
    perksAllowed = false,
    robotsAllowed = false
  )

  ///////////////////////////////////////////////////////////////////////////
  //          Public Functions
  ///////////////////////////////////////////////////////////////////////////

  $scope.searchTerm = null

  $scope.initGames = () => contestSearch(searchOptions)

  $scope.getSelectedContest = () => selectedContest

  $scope.searchResults = () => searchResults

  $scope.searchOptions = () => searchOptions

  $scope.enterGame = (contest: js.Dynamic) => {
    if (isDefined(contest) && isParticipant(contest)) {
      mySession.setContest_@(contest)
      $location.path(s"/dashboard/${contest.OID}")
    }
    else {
      toaster.pop("error", "You must join the contest first")
    }
  }

  $scope.invitePlayerPopup = (contest: js.Dynamic, playerID: String) => {
    contestService.findPlayerByID_@(contest, playerID) match {
      case Some(participant) => invitePlayerDialog.popup($scope, participant)
      case _ =>
        toaster.pop("error", "You must join tghe game to use this feature", null)
    }
  }

  $scope.getAvailableCount = () => searchResults.count(_.status === "ACTIVE")

  $scope.getAvailableSlots = (contest: js.Dynamic, rowIndex: js.UndefOr[Number]) => getAvailableSlots(contest, rowIndex)

  private def getAvailableSlots(contest: js.Dynamic, rowIndex: js.UndefOr[Number]): js.Array[js.Dynamic] = {
    if (!isDefined(contest) || !isDefined(contest.participants)) emptyArray[js.Dynamic]
    else {
      val row = rowIndex.map(_.intValue()) getOrElse 0
      val participants = contest.participants.asArray[js.Dynamic]
      val start = row * 8 // 0=0, 1=8, 2=16
      val end = row * 8 + 8 // 0=7, 1=8, 2=15

      val slots = emptyArray[js.Dynamic]
      (start to end) foreach (n => slots.push(if (n < participants.length) participants(n) else null))
      slots
    }
  }

  $scope.contestSearch = (searchOptions: js.Dynamic) => contestSearch(searchOptions)

  def contestSearch(searchOptions: js.Dynamic) = {
    $scope.startLoading()
    g.console.log(s"searchOptions = ${JSON.stringify(searchOptions)}")
    contestService.findContests_@(searchOptions)
      .success { contests: js.Array[js.Dynamic] =>
      searchResults = contests
      $scope.stopLoading()
    }.error { err: HttpError =>
      toaster.pop("error", "Error!", "Failed to execute Contest Search")
      $scope.stopLoading()
    }
  }

  $scope.getSearchResults = (searchTerm: js.UndefOr[String]) => {
    searchTerm map { mySearchTerm  =>
      if(mySearchTerm != null) {
        val term = mySearchTerm.trim.toLowerCase
        searchResults.filter(_.name.as[String].toLowerCase.contains(term))
      } else searchResults
    } getOrElse searchResults
  }

  $scope.contestStatusClass = (contest: js.Dynamic) => {
    if (!isDefined(contest)) ""
    else if (contest.status === "ACTIVE") "positive"
    else if (contest.status === "CLOSED") "negative"
    else ""
  }

  $scope.getStatusClass = (c: js.Dynamic) => {
    if (!isDefined(c) || !isDefined(c.participants)) ""
    else {
      val playerCount = c.participants.asArray[js.Dynamic].length
      if (playerCount + 1 < MaxPlayers) "positive"
      else if (playerCount + 1 == MaxPlayers) "warning"
      else if (playerCount >= MaxPlayers) "negative"
      else if (c.status === "ACTIVE") "positive"
      else if (c.status === "CLOSED") "negative"
      else "null"
    }
  }

  $scope.trophy = (place: String) => place match {
    case "1st" => "contests/gold.png"
    case "2nd" => "contests/silver.png"
    case "3rd" => "contests/bronze.png"
    case _ => "status/transparent.png"
  }

  ///////////////////////////////////////////////////////////////////////////
  //          Contest Selection Functions
  ///////////////////////////////////////////////////////////////////////////

  $scope.isSplitScreen = () => splitScreen && (selectedContest != null)

  $scope.selectContest = (contest: js.Dynamic) => {
    if (isDefined(contest)) {
      g.console.log(s"Selecting contest '${contest.name}' (${contest.OID})")
      selectedContest = contest
      splitScreen = true

      if (!isDefined(contest.rankings)) {
        contestService.getPlayerRankings_@(contest, mySession.userProfile.name.as[String])
      }
    }
  }

  $scope.toggleSplitScreen = () => splitScreen = false

  private def isContestSelected(contestId: String) = (selectedContest != null) && (selectedContest.OID == contestId)

  ///////////////////////////////////////////////////////////////////////////
  //          Contest Management Functions
  ///////////////////////////////////////////////////////////////////////////

  $scope.containsPlayer = (contest: js.Dynamic, userProfile: js.Dynamic) => {
    isDefined(contest) && isDefined(userProfile._id) && contestService.findPlayerByID_@(contest, userProfile.OID).isDefined
  }

  $scope.isContestOwner = (contest: js.Dynamic) => isContestOwner(contest)

  private def isContestOwner(contest: js.Dynamic) = {
    isDefined(contest) && isDefined(contest.creator) && isDefined(contest.creator.name) && contest.creator.name == mySession.userProfile.name
  }

  $scope.isDeletable = { (contest: js.Dynamic) =>
    isContestOwner(contest) && (!isDefined(contest.startTime) || contest.participants.asArray[js.Dynamic].length == 1)
  }

  $scope.isParticipant = (contest: js.Dynamic) => isParticipant(contest)

  private def isParticipant(contest: js.Dynamic) = {
    isDefined(contest) && isDefined(contest.participants) &&
      contest.participants.asArray[js.Dynamic].exists(_.OID == mySession.userProfile.OID)
  }

  $scope.deleteContest = { (contest: js.Dynamic) =>
    contest.deleting = true
    g.console.log(s"Deleting contest ${contest.name}...")
    contestService.deleteContest_@(contest.OID)
      .success { (response: js.Dynamic) =>
      //removeContestFromList(contest.OID)
      $scope.contestSearch($scope.searchOptions)
      $timeout(() => contest.deleting = false, 500)

    }.error { (err: HttpError) =>
      toaster.pop("error", "Error!", "Failed to delete contest")
      g.console.error("An error occurred while deleting the contest")
      $timeout(() => contest.deleting = false, 500)
    }
  }

  $scope.isJoinable = { (contest: js.Dynamic) =>
    mySession.isAuthenticated_@ && isDefined(contest) &&
      (!isDefined(contest.invitationOnly) || !contest.invitationOnly.as[Boolean]) &&
      !isContestOwner(contest) && !isParticipant(contest)
  }

  $scope.joinContest = { (contest: js.Dynamic) =>
    contest.joining = true
    val playerInfo = JS(player = JS(id = mySession.userProfile.OID, name = mySession.userProfile.name, facebookID = mySession.facebookID_@))
    contestService.joinContest_@(contest.OID, playerInfo)
      .success { (contest: js.Dynamic) =>
      if (!js.isUndefined(contest.error)) {
        toaster.pop("error", contest.error.as[String], null)
        g.console.error(contest.error)
      }
      else {
        $scope.contest = contest
        mySession.setContest_@(contest)
        //mySession.deduct(contest.startingBalance)
        //updateWithRankings(user.name, contest)
      }

      $timeout(() => contest.joining = false, 500)

    }.error { (err: HttpError) =>
      toaster.pop("error", "Error!", "Failed to join contest")
      g.console.error("An error occurred while joining the contest")
      $timeout(() => contest.joining = false, 500)
    }
  }

  $scope.joinedParticipant = (contest: js.Dynamic, userProfile: js.Dynamic) => $scope.containsPlayer(contest, userProfile)

  $scope.quitContest = { (contest: js.Dynamic) =>
    contest.quitting = true
    contestService.quitContest_@(contest.OID, mySession.userProfile.OID)
      .success { (updatedContest: js.Dynamic) =>
      if (js.isUndefined(updatedContest.error)) {
        $scope.contest = updatedContest
        mySession.setContest_@(updatedContest)
      }
      else {
        g.console.error("error = " + updatedContest.error)
        toaster.pop("error", "Unable to process your quit command at this time.", null)
      }
      $timeout(() => contest.quitting = false, 500)

    }.error { (err: HttpError) =>
      g.console.error("An error occurred while joining the contest")
      $timeout(() => contest.quitting = false, 500)
    }
  }

  $scope.startContest = { (contest: js.Dynamic) =>
    contest.starting = true
    contestService.startContest_@(contest.OID)
      .success { (contest: js.Dynamic) =>
      if (!js.isUndefined(contest.error)) {
        toaster.pop("error", contest.error.as[String], null)
        g.console.error(contest.error)
      }

      $timeout(() => contest.starting = false, 500)

    }.error { err: HttpError =>
      g.console.error("An error occurred while starting the contest")
      $timeout(() => contest.starting = false, 500)
    }
  }

  //////////////////////////////////////////////////////////////////////
  //              Style/CSS Functions
  //////////////////////////////////////////////////////////////////////

  $scope.getSelectionClass = { (c: js.Dynamic) =>
    if ((selectedContest != null) && (selectedContest.OID == c.OID)) "selected"
    else if (c.status === "ACTIVE") ""
    else "null"
  }

  //////////////////////////////////////////////////////////////////////
  //              Broadcast Event Listeners
  //////////////////////////////////////////////////////////////////////

  private def indexOfContest(contestId: String) = searchResults.indexWhere(_.OID == contestId)

  private def updateContestInList(searchResults: js.Array[js.Dynamic], contestId: String) {
    val index = searchResults.indexWhere(_.OID == contestId)
    if (index != -1) {
      contestService.getContestByID_@(contestId)
        .success { loadedContest: js.Dynamic => searchResults(index) = loadedContest }
        .error { err: HttpError =>
        g.console.error("Error selecting feed: " + err.getMessage)
        toaster.pop("error", "Error loading game", null)
      }
    }
  }

  private def removeContestFromList(searchResults: js.Array[_], contestId: String) {
    val index = indexOfContest(contestId)
    if (index != -1) {
      g.console.log(s"Removed contest $contestId from the list...")
      searchResults.splice(index, 1)
    }

    if (selectedContest != null && selectedContest.OID == contestId) {
      selectedContest = null
    }
  }

  ///////////////////////////////////////////////////////////////////////////
  //          Event Listeners
  ///////////////////////////////////////////////////////////////////////////

  /**
   * Listen for contest creation events
   */
  $scope.$on("contest_created", { (event: js.Dynamic, contest: js.Dynamic) =>
    g.console.log(s"New contest created '${contest.name}'")
    searchResults.push(contest)
    //mySession.refresh()
  })

  /**
   * Listen for contest deletion events
   */
  $scope.$on("contest_deleted", { (event: js.Dynamic, contest: js.Dynamic) =>
    g.console.log(s"Contest '${contest.name}' deleted")
    selectedContest = null
    searchResults.push(contest)
    //mySession.refresh()
  })

  /**
   * Listen for contest update events
   */
  $scope.$on("contest_updated", { (event: js.Dynamic, contest: js.Dynamic) =>
    g.console.log(s"Contest '${contest.name} updated")
    var contestId = contest.OID

    // update the contest in our search results
    updateContestInList(searchResults, contestId)

    // make sure we"re pointing at the updated contest
    if (isContestSelected(contestId)) {
      selectedContest = contest
    }
  })

}
