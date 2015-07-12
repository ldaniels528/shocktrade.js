package com.shocktrade.javascript.dashboard

import com.github.ldaniels528.scalascript.core.{Location, Timeout}
import com.github.ldaniels528.scalascript.extensions.Toaster
import com.github.ldaniels528.scalascript.{Scope, angular, injected, scoped}
import com.shocktrade.javascript.AppEvents._
import com.shocktrade.javascript.ScalaJsHelper._
import com.shocktrade.javascript.dialogs.InvitePlayerDialogService
import com.shocktrade.javascript.models.Contest.MaxPlayers
import com.shocktrade.javascript.models.{Contest, ContestSearchOptions}
import com.shocktrade.javascript.{GlobalLoading, MySession}
import org.scalajs.dom.console

import scala.language.postfixOps
import scala.scalajs.concurrent.JSExecutionContext.Implicits.runNow
import scala.scalajs.js
import scala.scalajs.js.Dynamic.{global => g, literal => JS}
import scala.util.{Failure, Success}

/**
 * Game Search Controller
 * @author lawrence.daniels@gmail.com
 */
class GameSearchController($scope: GameSearchScope, $location: Location, $timeout: Timeout, toaster: Toaster,
                           @injected("ContestService") contestService: ContestService,
                           @injected("InvitePlayerDialog") invitePlayerDialog: InvitePlayerDialogService,
                           @injected("MySession") mySession: MySession)
  extends GameController($scope, $location, toaster, mySession) with GlobalLoading {

  // public variables
  var searchResults = js.Array[Contest]()
  var selectedContest: Option[Contest] = None
  var splitScreen = false

  $scope.contest = null
  $scope.searchTerm = null
  $scope.searchOptions = ContestSearchOptions()

  ///////////////////////////////////////////////////////////////////////////
  //          Public Functions
  ///////////////////////////////////////////////////////////////////////////

  @scoped def getSearchResults = searchResults

  @scoped def getSelectedContest = selectedContest.orNull

  @scoped
  def invitePlayerPopup(contest: Contest, playerID: String) {
    mySession.findPlayerByID(contest, playerID) match {
      case Some(participant) =>
        invitePlayerDialog.popup(participant)
      case _ =>
        toaster.error("You must join the game to use this feature")
    }
  }

  @scoped def getAvailableCount = searchResults.count(_.status == "ACTIVE")

  @scoped
  def getAvailableSlots(contest: Contest, rowIndex: js.UndefOr[Number]) = {
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

  @scoped
  def contestSearch(searchOptions: ContestSearchOptions) = {
    asyncLoading($scope)(contestService.findContests(searchOptions)) onComplete {
      case Success(contests) =>
        searchResults = contests
      case Failure(e) =>
        toaster.error("Failed to execute Contest Search")
        console.error(s"Failed: searchOptions = ${angular.toJson(searchOptions)}")
    }
  }

  @scoped
  def getSearchResults(searchTerm: js.UndefOr[String]) = {
    searchTerm map { mySearchTerm =>
      if (mySearchTerm != null) {
        val term = mySearchTerm.trim.toLowerCase
        searchResults.filter(_.name.toLowerCase.contains(term))
      } else searchResults
    } getOrElse searchResults
  }

  @scoped
  def contestStatusClass(contest: Contest) = {
    if (!isDefined(contest)) ""
    else if (contest.status == "ACTIVE") "positive"
    else if (contest.status == "CLOSED") "negative"
    else ""
  }

  @scoped
  def getStatusClass(c: Contest) = {
    if (!isDefined(c) || !isDefined(c.participants)) ""
    else {
      val playerCount = c.participants.asArray[js.Dynamic].length
      if (playerCount + 1 < MaxPlayers) "positive"
      else if (playerCount + 1 == MaxPlayers) "warning"
      else if (playerCount >= MaxPlayers) "negative"
      else if (c.status == "ACTIVE") "positive"
      else if (c.status == "CLOSED") "negative"
      else "null"
    }
  }

  @scoped
  def trophy(place: js.UndefOr[String]) = place map {
    case "1st" => "contests/gold.png"
    case "2nd" => "contests/silver.png"
    case "3rd" => "contests/bronze.png"
    case _ => "status/transparent.png"
  }

  ///////////////////////////////////////////////////////////////////////////
  //          Contest Selection Functions
  ///////////////////////////////////////////////////////////////////////////

  @scoped def isSplitScreen = splitScreen && selectedContest.isDefined

  @scoped def toggleSplitScreen() = splitScreen = false

  @scoped
  def selectContest(contest: Contest) = {
    if (isDefined(contest)) {
      contest.OID_? foreach { contestId =>
        console.log(s"Selecting contest '${contest.name}' ($contestId)")
        selectedContest = Option(contest)
        splitScreen = true

        if (!isDefined(contest.rankings)) {
          contestService.getPlayerRankings(contest, mySession.getUserID)
        }
      }
    }
  }

  private def isContestSelected(contestId: String) = selectedContest.exists(_.OID_?.contains(contestId))

  ///////////////////////////////////////////////////////////////////////////
  //          Contest Management Functions
  ///////////////////////////////////////////////////////////////////////////

  @scoped
  def containsPlayer(contest: Contest, userProfile: js.Dynamic) = {
    isDefined(contest) && userProfile.OID_?.exists(mySession.findPlayerByID(contest, _).isDefined)
  }

  @scoped
  def isDeletable(contest: Contest) = {
    isDefined(contest) && isDefined(contest.creator) && contest.creator.name == mySession.userProfile.name &&
      (!isDefined(contest.startTime) || contest.participants.length == 1)
  }

  @scoped
  def isContestOwner(contest: Contest) = {
    isDefined(contest) && isDefined(contest.creator) && contest.creator.name == mySession.userProfile.name
  }

  @scoped
  def deleteContest(contest: Contest) = {
    contest.OID_? foreach { contestId =>
      contest.dynamic.deleting = true
      console.log(s"Deleting contest ${contest.name}...")
      asyncLoading($scope)(contestService.deleteContest(contestId)) onComplete {
        case Success(response) =>
          removeContestFromList(searchResults, contestId)
          $timeout(() => contest.dynamic.deleting = false, 500)
        case Failure(e) =>
          toaster.error("Error!", "Failed to delete contest")
          g.console.error("An error occurred while deleting the contest")
          $timeout(() => contest.dynamic.deleting = false, 500)
      }
    }
  }

  @scoped
  def isJoinable(contest: Contest) = {
    mySession.isAuthenticated && isDefined(contest) &&
      (!isDefined(contest.invitationOnly) || !contest.invitationOnly) &&
      !(isDefined(contest.creator) && contest.creator.name == mySession.userProfile.name) && // !isContestOwner(...)
      !hasParticipant(contest)
  }

  @scoped
  def joinContest(contest: Contest) = {
    for {
      contestId <- contest.OID_?
      userId <- mySession.userProfile.OID_?
    } {
      contest.dynamic.joining = true
      val playerInfo = JS(player = JS(id = userId, name = mySession.userProfile.name, facebookID = mySession.getFacebookID))
      asyncLoading($scope)(contestService.joinContest(contestId, playerInfo)) onComplete {
        case Success(joinedContest) =>
          $scope.contest = joinedContest
          mySession.setContest(joinedContest)
          //mySession.deduct(contest.startingBalance)
          //updateWithRankings(user.name, contest)
          $timeout(() => joinedContest.dynamic.joining = false, 500)

        case Failure(e) =>
          toaster.error("Error!", "Failed to join contest")
          g.console.error("An error occurred while joining the contest")
          $timeout(() => contest.dynamic.joining = false, 500)
      }
    }
  }

  @scoped
  def quitContest(contest: Contest) = {
    contest.OID_? foreach { contestId =>
      contest.dynamic.quitting = true
      asyncLoading($scope)(contestService.quitContest(contestId, mySession.getUserID)) onComplete {
        case Success(updatedContest) =>
          $scope.contest = updatedContest
          mySession.setContest(updatedContest)
          $timeout(() => contest.dynamic.quitting = false, 500)

        case Failure(e) =>
          g.console.error("An error occurred while joining the contest")
          $timeout(() => contest.dynamic.quitting = false, 500)
      }
    }
  }

  @scoped
  def startContest(contest: Contest) = {
    contest.OID_? foreach { contestId =>
      contest.dynamic.starting = true
      asyncLoading($scope)(contestService.startContest(contestId)) onComplete {
        case Success(theContest) =>
          if (!js.isUndefined(theContest.dynamic.error)) {
            toaster.error(theContest.dynamic.error)
            g.console.error(theContest.dynamic.error)
          }
          $timeout(() => theContest.dynamic.starting = false, 500)

        case Failure(e) =>
          toaster.error("An error occurred while starting the contest")
          g.console.error("An error occurred while starting the contest")
          $timeout(() => contest.dynamic.starting = false, 500)
      }
    }
  }

  //////////////////////////////////////////////////////////////////////
  //              Style/CSS Functions
  //////////////////////////////////////////////////////////////////////

  @scoped
  def getSelectionClass(c: Contest) = {
    if (selectedContest.exists(_.OID_?.exists(c.OID_?.contains))) "selected"
    else if (c.status == "ACTIVE") ""
    else "null"
  }

  //////////////////////////////////////////////////////////////////////
  //              Broadcast Event Listeners
  //////////////////////////////////////////////////////////////////////

  private def indexOfContest(contestId: String) = searchResults.indexWhere(_.OID_?.contains(contestId))

  private def updateContestInList(searchResults: js.Array[Contest], contestId: String) {
    val index = searchResults.indexWhere(_.OID_?.contains(contestId))
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

    if (selectedContest.exists(_.OID_?.contains(contestId))) selectedContest = None
  }

  ///////////////////////////////////////////////////////////////////////////
  //          Event Listeners
  ///////////////////////////////////////////////////////////////////////////

  /**
   * Listen for contest creation events
   */
  $scope.$on(ContestCreated, { (event: js.Dynamic, contest: Contest) =>
    console.log(s"New contest created '${contest.name}'")
    searchResults.push(contest)
    //mySession.refresh()
  })

  /**
   * Listen for contest deletion events
   */
  $scope.$on(ContestDeleted, { (event: js.Dynamic, contest: Contest) =>
    console.log(s"Contest '${contest.name}' deleted")
    selectedContest = None
    searchResults = searchResults.filterNot(_.OID_?.exists(contest.OID_?.contains))
  })

  /**
   * Listen for contest update events
   */
  $scope.$on(ContestUpdated, { (event: js.Dynamic, contest: Contest) =>
    console.log(s"Contest '${contest.name} updated")
    contest.OID_? foreach { contestId =>
      // update the contest in our search results
      updateContestInList(searchResults, contestId)

      // make sure we"re pointing at the updated contest
      if (isContestSelected(contestId)) selectedContest = Option(contest)
    }
  })

}

/**
 * Game Search Controller
 */
trait GameSearchScope extends Scope {
  var contest: Contest = js.native
  var searchTerm: String = js.native
  var searchOptions: ContestSearchOptions = js.native
}

