package com.shocktrade.javascript.dashboard

import com.shocktrade.javascript.{ScalaJsHelper, MySession}
import ScalaJsHelper._
import com.ldaniels528.scalascript.core.Location
import com.ldaniels528.scalascript.extensions.Toaster
import com.ldaniels528.scalascript.Controller
import com.shocktrade.javascript.MySession

import scala.scalajs.js

/**
 * Game Controller Trait
 * @author lawrence.daniels@gmail.com
 */
abstract class GameController($scope: js.Dynamic, $location: Location, toaster: Toaster, mySession: MySession)
  extends Controller {

  def enterGame(contest: js.Dynamic) {
    if (isDefined(contest) && isParticipant(contest)) {
      mySession.setContest(contest)
      $location.path(s"/dashboard/${contest.OID}")
    }
    else {
      toaster.error("You must join the contest first")
    }
  }

  protected def isParticipant(contest: js.Dynamic) = {
    isDefined(contest) && isDefined(contest.participants) &&
      contest.participants.asArray[js.Dynamic].exists(_.OID == mySession.userProfile.OID)
  }

}
