package com.shocktrade.javascript.dashboard

import com.shocktrade.javascript.MySessionService
import com.shocktrade.javascript.models.contest.Contest
import org.scalajs.angularjs.toaster.Toaster
import org.scalajs.angularjs.{Controller, Location, Scope}
import org.scalajs.nodejs.util.ScalaJsHelper._

import scala.scalajs.js

/**
  * Abstract Game Controller
  * @author lawrence.daniels@gmail.com
  */
abstract class GameController($scope: GameScope, $location: Location, toaster: Toaster, mySession: MySessionService)
  extends Controller {

  $scope.enterGame = (aContest: js.UndefOr[Contest]) => aContest foreach { contest =>
    if ($scope.isParticipant(contest)) {
      contest._id foreach { contestId =>
        mySession.setContest(contest)
        $location.path(s"/dashboard/$contestId")
      }
    }
    else {
      toaster.error("You must join the contest first")
    }
  }

  $scope.isParticipant = (aContest: js.UndefOr[Contest]) => aContest exists { contest =>
    contest.participants.exists(_.exists(_._id ?== mySession.userProfile._id))
  }

}

/**
  * Game Scope
  * @author lawrence.daniels@gmail.com
  */
@js.native
trait GameScope extends Scope {
  // functions
  var enterGame: js.Function1[js.UndefOr[Contest], Unit] = js.native
  var isParticipant: js.Function1[js.UndefOr[Contest], Boolean] = js.native
}
