package com.shocktrade.javascript.dashboard

import com.github.ldaniels528.scalascript.core.Location
import com.github.ldaniels528.scalascript.extensions.Toaster
import com.github.ldaniels528.scalascript.{Controller, Scope, scoped}
import com.shocktrade.javascript.MySession
import com.shocktrade.javascript.ScalaJsHelper._
import com.shocktrade.javascript.models.Contest

/**
 * Abstract Game Controller
 * @author lawrence.daniels@gmail.com
 */
abstract class GameController($scope: Scope, $location: Location, toaster: Toaster, mySession: MySession)
  extends Controller {

  @scoped
  def enterGame(contest: Contest) {
    if (hasParticipant(contest)) {
      contest.OID_?.foreach { contestId =>
        mySession.setContest(contest)
        $location.path(s"/dashboard/$contestId")
      }
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
