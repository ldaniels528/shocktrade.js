package com.shocktrade.javascript.dashboard

import biz.enef.angulate.ScopeController
import com.ldaniels528.javascript.angularjs.core.Location
import com.ldaniels528.javascript.angularjs.extensions.Toaster
import com.shocktrade.javascript.MySession
import com.shocktrade.javascript.ScalaJsHelper._

import scala.scalajs.js

/**
 * Game Controller Trait
 * @author lawrence.daniels@gmail.com
 */
abstract class GameController($scope: js.Dynamic, $location: Location, toaster: Toaster, mySession: MySession)
  extends ScopeController {

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
