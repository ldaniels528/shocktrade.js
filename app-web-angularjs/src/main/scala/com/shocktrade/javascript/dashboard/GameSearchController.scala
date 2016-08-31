package com.shocktrade.javascript.dashboard

import com.shocktrade.javascript.AppEvents._
import com.shocktrade.javascript.dialogs.InvitePlayerDialog
import com.shocktrade.javascript.forms.PlayerInfoForm
import com.shocktrade.javascript.models.contest.Contest.MaxPlayers
import com.shocktrade.javascript.models.contest.{Contest, ContestSearchOptions, Participant, PlayerInfo}
import com.shocktrade.javascript.models._
import com.shocktrade.javascript.{GlobalLoading, MySessionService}
import org.scalajs.angularjs.toaster.Toaster
import org.scalajs.angularjs.{Location, Timeout, angular, injected, _}
import org.scalajs.dom.{Event, console}
import org.scalajs.nodejs.util.ScalaJsHelper._

import scala.concurrent.duration._
import scala.language.postfixOps
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js
import scala.util.{Failure, Success}

/**
  * Game Search Controller
  * @author lawrence.daniels@gmail.com
  */
class GameSearchController($scope: GameSearchScope, $location: Location, $timeout: Timeout, toaster: Toaster,
                           @injected("ContestService") contestService: ContestService,
                           @injected("InvitePlayerDialog") invitePlayerDialog: InvitePlayerDialog,
                           @injected("MySessionService") mySession: MySessionService)
  extends GameController($scope, $location, toaster, mySession) with GlobalLoading {

  // public variables
  var searchResults = js.Array[Contest]()
  var selectedContest: js.UndefOr[Contest] = js.undefined
  var splitScreen = false

  $scope.contest = null
  $scope.searchTerm = null
  $scope.searchOptions = new ContestSearchOptions()

  ///////////////////////////////////////////////////////////////////////////
  //          Public Functions
  ///////////////////////////////////////////////////////////////////////////

  $scope.getSelectedContest = () => selectedContest

  $scope.invitePlayerPopup = (aContest: js.UndefOr[Contest], aPlayerID: js.UndefOr[String]) => {
    for {
      contest <- aContest
      playerID <- aPlayerID
    } {
      mySession.findPlayerByID(contest, playerID) match {
        case Some(participant) =>
          invitePlayerDialog.popup(participant)
        case _ =>
          toaster.error("You must join the game to use this feature")
      }
    }
  }

  $scope.getAvailableCount = () => searchResults.count(_.status == "ACTIVE")

  $scope.getAvailableSlots = (aContest: js.UndefOr[Contest], aRowIndex: js.UndefOr[Number]) => aContest map { contest =>
    val row = aRowIndex.map(_.intValue()) getOrElse 0
    val participants = contest.participants getOrElse emptyArray
    val start = row * 8
    val end = start + 8

    // generate the slots
    js.Array((start to end) map {
      case n if n < participants.length => participants(n)
      case _ => null
    }: _*)
  }

  $scope.contestSearch = (aSearchOptions: js.UndefOr[ContestSearchOptions]) => aSearchOptions foreach { searchOptions =>
    asyncLoading($scope)(contestService.findContests(searchOptions)) onComplete {
      case Success(contests) =>
        searchResults = contests
      case Failure(e) =>
        toaster.error("Failed to execute Contest Search")
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

  $scope.contestStatusClass = (aContest: js.UndefOr[Contest]) => aContest map {
    case contest if contest.status.contains("ACTIVE") => "positive"
    case contest if contest.status.contains("CLOSED") => "negative"
    case _ => ""
  }

  $scope.getStatusClass = (aContest: js.UndefOr[Contest]) => {
    aContest.toOption match {
      case Some(c) =>
        val playerCount = c.participants.map(_.length) getOrElse 0
        if (c.participants.isEmpty) ""
        else if (playerCount + 1 < MaxPlayers) "positive"
        else if (playerCount + 1 == MaxPlayers) "warning"
        else if (playerCount >= MaxPlayers) "negative"
        else if (c.status.contains("ACTIVE")) "positive"
        else if (c.status.contains("CLOSED")) "negative"
        else "null"
      case None => ""
    }
  }

  $scope.trophy = (aPlace: js.UndefOr[String]) => aPlace map {
    case "1st" => "contests/gold.png"
    case "2nd" => "contests/silver.png"
    case "3rd" => "contests/bronze.png"
    case _ => "status/transparent.png"
  }

  ///////////////////////////////////////////////////////////////////////////
  //          Contest Selection Functions
  ///////////////////////////////////////////////////////////////////////////

  $scope.isSplitScreen = () => splitScreen && selectedContest.isDefined

  $scope.toggleSplitScreen = () => splitScreen = false

  $scope.selectContest = (aContest: js.UndefOr[Contest]) => {
    selectedContest = aContest
    for {
      contest <- aContest
      contestId <- contest._id
    } {
      console.log(s"Selecting contest '${contest.name}' ($contestId)")
      splitScreen = true

      if (contest.rankings.nonEmpty) {
        mySession.userProfile._id.foreach(contestService.getPlayerRankings(contest, _))
      }
    }
  }

  private def isContestSelected(contestId: String) = selectedContest.exists(_._id.contains(contestId))

  ///////////////////////////////////////////////////////////////////////////
  //          Contest Management Functions
  ///////////////////////////////////////////////////////////////////////////

  $scope.containsPlayer = (aContest: js.UndefOr[Contest], aUserProfile: js.UndefOr[UserProfile]) => {
    (for {
      contest <- aContest
      userProfile <- aUserProfile
      uid <- userProfile._id
    } yield mySession.findPlayerByID(contest, uid)).isDefined
  }

  $scope.isDeletable = (aContest: js.UndefOr[Contest]) => aContest exists { contest =>
    isDefined(contest.creator) && contest.creator == mySession.userProfile._id &&
      (!isDefined(contest.startTime) || contest.participants.exists(_.length == 1))
  }

  $scope.isContestOwner = (aContest: js.UndefOr[Contest]) => aContest exists { contest =>
    isDefined(contest.creator) && contest.creator == mySession.userProfile._id
  }

  $scope.deleteContest = (aContest: js.UndefOr[Contest]) => {
    for {
      contest <- aContest
      contestId <- contest._id.toOption
    } {
      contest.deleting = true
      console.log(s"Deleting contest ${contest.name}...")
      asyncLoading($scope)(contestService.deleteContest(contestId)) onComplete {
        case Success(response) =>
          removeContestFromList(searchResults, contestId)
          $timeout(() => contest.deleting = false, 0.5.seconds)
        case Failure(e) =>
          toaster.error("Error!", "Failed to delete contest")
          console.error("An error occurred while deleting the contest")
          $timeout(() => contest.deleting = false, 0.5.seconds)
      }
    }
  }

  $scope.isJoinable = (aContest: js.UndefOr[Contest]) => aContest exists { contest =>
    mySession.isAuthenticated && isDefined(contest) &&
      (!contest.invitationOnly.isTrue) &&
      !(isDefined(contest.creator) && contest.creator == mySession.userProfile._id) && // !isContestOwner(...)
      !$scope.isParticipant(contest)
  }

  $scope.joinContest = (aContest: js.UndefOr[Contest]) => {
    for {
      contest <- aContest
      contestId <- contest._id.toOption
      facebookID <- mySession.facebookID
      userId <- mySession.userProfile._id.toOption
    } {
      contest.joining = true
      val playerInfo = new PlayerInfoForm(player = new PlayerInfo(_id = userId, name = mySession.userProfile.name, facebookID = facebookID))
      asyncLoading($scope)(contestService.joinContest(contestId, playerInfo)) onComplete {
        case Success(joinedContest) =>
          $scope.contest = joinedContest
          mySession.setContest(joinedContest)
          //mySession.deduct(contest.startingBalance)
          //updateWithRankings(user.name, contest)
          $timeout(() => contest.joining = false, 0.5.seconds)

        case Failure(e) =>
          toaster.error("Error!", "Failed to join contest")
          console.error("An error occurred while joining the contest")
          $timeout(() => contest.joining = false, 0.5.seconds)
      }
    }
  }

  $scope.quitContest = (aContest: js.UndefOr[Contest]) => {
    for {
      contest <- aContest
      userId <- mySession.userProfile._id.toOption
      contestId <- contest._id.toOption
    } {
      contest.quitting = true
      asyncLoading($scope)(contestService.quitContest(contestId, userId)) onComplete {
        case Success(updatedContest) =>
          $scope.contest = updatedContest
          mySession.setContest(updatedContest)
          $timeout(() => contest.quitting = false, 0.5.seconds)

        case Failure(e) =>
          console.error("An error occurred while joining the contest")
          $timeout(() => contest.quitting = false, 0.5.seconds)
      }
    }
  }

  $scope.startContest = (aContest: js.UndefOr[Contest]) => {
    for {
      contest <- aContest
      contestId <- contest._id
    } {
      contest.starting = true
      asyncLoading($scope)(contestService.startContest(contestId)) onComplete {
        case Success(theContest) =>
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

  $scope.getSelectionClass = (aContest: js.UndefOr[Contest]) => aContest map { c =>
    if (selectedContest.exists(_._id ?== c._id)) "selected"
    else if (c.status ?== "ACTIVE") ""
    else "null"
  }

  //////////////////////////////////////////////////////////////////////
  //              Broadcast Event Listeners
  //////////////////////////////////////////////////////////////////////

  private def indexOfContest(contestId: String) = searchResults.indexWhere(_._id.contains(contestId))

  private def updateContestInList(searchResults: js.Array[Contest], contestId: String) {
    val index = searchResults.indexWhere(_._id.contains(contestId))
    if (index != -1) {
      asyncLoading($scope)(contestService.getContestByID(contestId)) onComplete {
        case Success(loadedContest) => searchResults(index) = loadedContest
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

    if (selectedContest.exists(_._id ?== contestId)) selectedContest = js.undefined
  }

  ///////////////////////////////////////////////////////////////////////////
  //          Event Listeners
  ///////////////////////////////////////////////////////////////////////////

  /**
    * Listen for contest creation events
    */
  $scope.$on(ContestCreated, { (event: Event, contest: Contest) =>
    console.log(s"New contest created '${contest.name}'")
    searchResults.push(contest)
    //mySession.refresh()
  })

  /**
    * Listen for contest deletion events
    */
  $scope.$on(ContestDeleted, { (event: Event, contest: Contest) =>
    console.log(s"Contest '${contest.name}' deleted")
    selectedContest = js.undefined
    searchResults = searchResults.filterNot(_._id ?== contest._id)
  })

  /**
    * Listen for contest update events
    */
  $scope.$on(ContestUpdated, { (event: Event, contest: Contest) =>
    console.log(s"Contest '${contest.name} updated")
    contest._id foreach { contestId =>
      // update the contest in our search results
      updateContestInList(searchResults, contestId)

      // make sure we"re pointing at the updated contest
      if (isContestSelected(contestId)) selectedContest = contest
    }
  })

}

/**
  * Game Search Controller Scope
  * @author lawrence.daniels@gmail.com
  */
@js.native
trait GameSearchScope extends GameScope {
  // variables
  var contest: Contest = js.native
  var searchTerm: String = js.native
  var searchOptions: ContestSearchOptions = js.native

  // functions
  var contestStatusClass: js.Function1[js.UndefOr[Contest], js.UndefOr[String]] = js.native
  var getAvailableCount: js.Function0[Int] = js.native
  var getAvailableSlots: js.Function2[js.UndefOr[Contest], js.UndefOr[Number], js.UndefOr[js.Array[Participant]]] = js.native
  var getSelectionClass: js.Function1[js.UndefOr[Contest], js.UndefOr[String]] = js.native
  var getStatusClass: js.Function1[js.UndefOr[Contest], String] = js.native
  var isSplitScreen: js.Function0[Boolean] = js.native
  var toggleSplitScreen: js.Function0[Unit] = js.native
  var trophy: js.Function1[js.UndefOr[String], js.UndefOr[String]] = js.native

  // contest functions
  var containsPlayer: js.Function2[js.UndefOr[Contest], js.UndefOr[UserProfile], Boolean] = js.native
  var contestSearch: js.Function1[js.UndefOr[ContestSearchOptions], Unit] = js.native
  var deleteContest: js.Function1[js.UndefOr[Contest], Unit] = js.native
  var getSearchResults: js.Function1[js.UndefOr[String], js.Array[Contest]] = js.native
  var getSelectedContest: js.Function0[js.UndefOr[Contest]] = js.native
  var invitePlayerPopup: js.Function2[js.UndefOr[Contest], js.UndefOr[String], Unit] = js.native
  var isContestOwner: js.Function1[js.UndefOr[Contest], Boolean] = js.native
  var isDeletable: js.Function1[js.UndefOr[Contest], Boolean] = js.native
  var isJoinable: js.Function1[js.UndefOr[Contest], Boolean] = js.native
  var joinContest: js.Function1[js.UndefOr[Contest], Unit] = js.native
  var quitContest: js.Function1[js.UndefOr[Contest], Unit] = js.native
  var selectContest: js.Function1[js.UndefOr[Contest], Unit] = js.native
  var startContest: js.Function1[js.UndefOr[Contest], Unit] = js.native

}

