package com.shocktrade.javascript.dashboard

import com.ldaniels528.scalascript.core.{Location, Timeout}
import com.ldaniels528.scalascript.extensions.Toaster
import com.ldaniels528.scalascript.{Scope, injected}
import com.shocktrade.javascript.AppEvents._
import com.shocktrade.javascript.ScalaJsHelper._
import com.shocktrade.javascript.dialogs.InvitePlayerDialogService
import com.shocktrade.javascript.{GlobalLoading, MySession}
import org.scalajs.dom.console

import scala.language.postfixOps
import scala.scalajs.concurrent.JSExecutionContext.Implicits.runNow
import scala.scalajs.js
import scala.scalajs.js.Dynamic.{global => g, literal => JS}
import scala.scalajs.js.JSON
import scala.util.{Failure, Success}

/**
 * Game Search Controller
 * @author lawrence.daniels@gmail.com
 */
class GameSearchController($scope: js.Dynamic, $location: Location, $timeout: Timeout, toaster: Toaster,
                           @injected("ContestService") contestService: ContestService,
                           @injected("InvitePlayerDialog") invitePlayerDialog: InvitePlayerDialogService,
                           @injected("MySession") mySession: MySession)
  extends GameController($scope, $location, toaster, mySession) with GlobalLoading {

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

  $scope.enterGame = (contest: js.Dynamic) => enterGame(contest)

  $scope.invitePlayerPopup = (contest: js.Dynamic, playerID: String) => {
    mySession.findPlayerByID(contest, playerID) match {
      case Some(participant) =>
        invitePlayerDialog.popup(participant)
      case _ =>
        toaster.error("You must join tghe game to use this feature")
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
    console.log(s"searchOptions = ${JSON.stringify(searchOptions)}")
    asyncLoading($scope)(contestService.findContests(searchOptions)) onComplete {
      case Success(contests) =>
        searchResults = contests
      case Failure(e) =>
        toaster.error("Failed to execute Contest Search")
    }
  }

  $scope.getSearchResults = (searchTerm: js.UndefOr[String]) => {
    searchTerm map { mySearchTerm =>
      if (mySearchTerm != null) {
        val term = mySearchTerm.trim.toLowerCase
        searchResults.filter(_.name.asOpt[String].map(_.toLowerCase).exists(_.contains(term)))
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

  $scope.trophy = (place: js.UndefOr[String]) => place map {
    case "1st" => "contests/gold.png"
    case "2nd" => "contests/silver.png"
    case "3rd" => "contests/bronze.png"
    case _ => "status/transparent.png"
  }

  ///////////////////////////////////////////////////////////////////////////
  //          Contest Selection Functions
  ///////////////////////////////////////////////////////////////////////////

  $scope.isSplitScreen = () => splitScreen && (selectedContest != null)

  $scope.selectContest = (contest: js.Dynamic) => selectContest(contest)

  $scope.toggleSplitScreen = () => splitScreen = false

  private def isContestSelected(contestId: String) = Option(selectedContest).exists(_.OID_?.contains(contestId))

  private def selectContest(contest: js.Dynamic) = {
    if (isDefined(contest)) {
      console.log(s"Selecting contest '${contest.name}' (${contest.OID})")
      selectedContest = contest
      splitScreen = true

      if (!isDefined(contest.rankings)) {
        contestService.getPlayerRankings(contest, mySession.getUserID)
      }
    }
  }

  ///////////////////////////////////////////////////////////////////////////
  //          Contest Management Functions
  ///////////////////////////////////////////////////////////////////////////

  $scope.containsPlayer = (contest: js.Dynamic, userProfile: js.Dynamic) => {
    isDefined(contest) && isDefined(userProfile._id) && mySession.findPlayerByID(contest, userProfile.OID).isDefined
  }

  $scope.isContestOwner = (contest: js.Dynamic) => isContestOwner(contest)

  $scope.isDeletable = { (contest: js.Dynamic) =>
    isContestOwner(contest) && (!isDefined(contest.startTime) || contest.participants.asArray[js.Dynamic].length == 1)
  }

  $scope.isParticipant = (contest: js.Dynamic) => isParticipant(contest)

  $scope.deleteContest = (contest: js.Dynamic) => deleteContest(contest)

  private def isContestOwner(contest: js.Dynamic) = {
    isDefined(contest) && isDefined(contest.creator) && isDefined(contest.creator.name) && contest.creator.name == mySession.userProfile.name
  }

  private def deleteContest(contest: js.Dynamic) = {
    contest.deleting = true
    console.log(s"Deleting contest ${contest.name}...")
    asyncLoading($scope)(contestService.deleteContest(contest.OID)) onComplete {
      case Success(response) =>
        removeContestFromList(searchResults, contest.OID)
        $timeout(() => contest.deleting = false, 500)
      case Failure(e) =>
        toaster.error("Error!", "Failed to delete contest")
        g.console.error("An error occurred while deleting the contest")
        $timeout(() => contest.deleting = false, 500)
    }
  }

  $scope.isJoinable = { (contest: js.Dynamic) =>
    mySession.isAuthenticated && isDefined(contest) &&
      (!isDefined(contest.invitationOnly) || !contest.invitationOnly.isTrue) &&
      !isContestOwner(contest) && !isParticipant(contest)
  }

  $scope.joinContest = (contest: js.Dynamic) => {
    contest.joining = true
    val playerInfo = JS(player = JS(id = mySession.userProfile.OID, name = mySession.userProfile.name, facebookID = mySession.getFacebookID))
    asyncLoading($scope)(contestService.joinContest(contest.OID, playerInfo)) onComplete {
      case Success(joinedContest) =>
        if (!js.isUndefined(joinedContest.error)) {
          toaster.error(joinedContest.error)
          g.console.error(joinedContest.error)
        }
        else {
          $scope.contest = joinedContest
          mySession.setContest(joinedContest)
          //mySession.deduct(contest.startingBalance)
          //updateWithRankings(user.name, contest)
        }
        $timeout(() => joinedContest.joining = false, 500)

      case Failure(e) =>
        toaster.error("Error!", "Failed to join contest")
        g.console.error("An error occurred while joining the contest")
        $timeout(() => contest.joining = false, 500)
    }
  }

  $scope.joinedParticipant = (contest: js.Dynamic, userProfile: js.Dynamic) => $scope.containsPlayer(contest, userProfile)

  $scope.quitContest = { (contest: js.Dynamic) =>
    contest.quitting = true
    asyncLoading($scope)(contestService.quitContest(contest.OID, mySession.getUserID)) onComplete {
      case Success(updatedContest) =>
        if (js.isUndefined(updatedContest.error)) {
          $scope.contest = updatedContest
          mySession.setContest(updatedContest)
        }
        else {
          g.console.error(s"error = ${updatedContest.error}")
          toaster.error("Unable to process your quit command at this time.")
        }
        $timeout(() => contest.quitting = false, 500)

      case Failure(e) =>
        g.console.error("An error occurred while joining the contest")
        $timeout(() => contest.quitting = false, 500)
    }
  }

  $scope.startContest = { (contest: js.Dynamic) =>
    contest.starting = true
    asyncLoading($scope)(contestService.startContest(contest.OID)) onComplete {
      case Success(theContest) =>
        if (!js.isUndefined(theContest.error)) {
          toaster.error(theContest.error)
          g.console.error(theContest.error)
        }
        $timeout(() => theContest.starting = false, 500)

      case Failure(e) =>
        toaster.error("An error occurred while starting the contest")
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
      asyncLoading($scope)(contestService.getContestByID(contestId)) onComplete {
        case Success(loadedContest) => searchResults(index) = loadedContest
        case Failure(e) =>
          g.console.error(s"Error selecting feed: ${e.getMessage}")
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
  scope.$on(ContestCreated, { (event: js.Dynamic, contest: js.Dynamic) =>
    console.log(s"New contest created '${contest.name}'")
    searchResults.push(contest)
    //mySession.refresh()
  })

  /**
   * Listen for contest deletion events
   */
  scope.$on(ContestDeleted, { (event: js.Dynamic, contest: js.Dynamic) =>
    console.log(s"Contest '${contest.name}' deleted")
    selectedContest = null
    searchResults = searchResults.filterNot(_.OID == contest.OID)
  })

  /**
   * Listen for contest update events
   */
  scope.$on(ContestUpdated, { (event: js.Dynamic, contest: js.Dynamic) =>
    console.log(s"Contest '${contest.name} updated")
    contest.OID_? foreach { contestId =>
      // update the contest in our search results
      updateContestInList(searchResults, contestId)

      // make sure we"re pointing at the updated contest
      if (isContestSelected(contestId)) {
        selectedContest = contest
      }
    }
  })

}
