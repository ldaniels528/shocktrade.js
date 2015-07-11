package com.shocktrade.javascript.dashboard

import com.ldaniels528.scalascript.core.Location
import com.ldaniels528.scalascript.extensions.Toaster
import com.ldaniels528.scalascript.{Controller, scoped}
import com.shocktrade.javascript.MySession
import com.shocktrade.javascript.ScalaJsHelper._
import com.shocktrade.javascript.models.Contest

import scala.scalajs.js

/**
 * Game Controller Trait
 * @author lawrence.daniels@gmail.com
 */
abstract class GameController($scope: js.Dynamic, $location: Location, toaster: Toaster, mySession: MySession)
  extends Controller {

  @scoped
  def enterGame(contest: Contest) {
    if ($scope.isParticipant(contest).as[Boolean]) {
      mySession.setContest(contest)
      contest.OID_?.foreach(contestId => $location.path(s"/dashboard/$contestId"))
    }
    else {
      toaster.error("You must join the contest first")
    }
  }

  @scoped
  def isParticipant(contest: Contest) = hasParticipant(contest)

  protected def hasParticipant(contest: Contest) = {
    isDefined(contest) && isDefined(contest.participants) &&
      contest.participants.exists(_.OID_?.exists(mySession.userProfile.OID_?.contains))
  }

}
