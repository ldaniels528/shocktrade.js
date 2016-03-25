package com.shocktrade.javascript.dashboard

import com.github.ldaniels528.scalascript.core.Location
import com.github.ldaniels528.scalascript.extensions.Toaster
import com.github.ldaniels528.scalascript.{Controller, Scope}
import com.shocktrade.javascript.MySessionService
import com.shocktrade.javascript.models.{BSONObjectID, Contest}

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
        $location.path(s"/dashboard/${contestId.$oid}")
      }
    }
    else {
      toaster.error("You must join the contest first")
    }
  }

  $scope.isParticipant = (aContest: js.UndefOr[Contest]) => aContest exists { contest =>
    contest.participants.exists(p => BSONObjectID.isEqual(p._id, mySession.userProfile._id))
  }

}

/**
  * Game Scope
  * @author lawrence.daniels@gmail.com
  */
@js.native
trait GameScope extends Scope {
  // functions
  var enterGame: js.Function1[js.UndefOr[Contest], Unit]
  var isParticipant: js.Function1[js.UndefOr[Contest], Boolean]
}
