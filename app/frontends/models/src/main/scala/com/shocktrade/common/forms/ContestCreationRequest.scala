package com.shocktrade.common.forms

import com.shocktrade.common.util.StringHelper._
import io.scalajs.util.JsUnderOrHelper._
import io.scalajs.util.ScalaJsHelper._

import scala.scalajs.js

/**
 * Contest Creation Request
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
class ContestCreationRequest(val contestID: js.UndefOr[String],
                             val name: js.UndefOr[String],
                             val userID: js.UndefOr[String],
                             val startingBalance: js.UndefOr[Double],
                             val startAutomatically: js.UndefOr[Boolean],
                             val duration: js.UndefOr[Int],
                             val friendsOnly: js.UndefOr[Boolean],
                             val invitationOnly: js.UndefOr[Boolean],
                             val levelCapAllowed: js.UndefOr[Boolean],
                             val levelCap: js.UndefOr[Int],
                             val perksAllowed: js.UndefOr[Boolean],
                             val robotsAllowed: js.UndefOr[Boolean]) extends js.Object

/**
 * Contest Creation Request
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
object ContestCreationRequest {

  def apply(contestID: js.UndefOr[String] = js.undefined,
            name: js.UndefOr[String] = js.undefined,
            userID: js.UndefOr[String] = js.undefined,
            startingBalance: js.UndefOr[Double] = js.undefined,
            startAutomatically: js.UndefOr[Boolean] = js.undefined,
            duration: js.UndefOr[Int] = js.undefined,
            friendsOnly: js.UndefOr[Boolean] = js.undefined,
            invitationOnly: js.UndefOr[Boolean] = js.undefined,
            levelCapAllowed: js.UndefOr[Boolean] = js.undefined,
            levelCap: js.UndefOr[Int] = js.undefined,
            perksAllowed: js.UndefOr[Boolean] = js.undefined,
            robotsAllowed: js.UndefOr[Boolean] = js.undefined): ContestCreationRequest = {
    new ContestCreationRequest(contestID, name, userID, startingBalance, startAutomatically, duration, friendsOnly, invitationOnly, levelCapAllowed, levelCap, perksAllowed, robotsAllowed)
  }

  /**
   * Contest Creation Request Extensions
   * @param request the given [[ContestCreationRequest request]]
   */
  final implicit class ContestCreationRequestExtensions(val request: ContestCreationRequest) extends AnyVal {

    def copy(contestID: js.UndefOr[String] = js.undefined,
             name: js.UndefOr[String] = js.undefined,
             userID: js.UndefOr[String] = js.undefined,
             startingBalance: js.UndefOr[Double] = js.undefined,
             startAutomatically: js.UndefOr[Boolean] = js.undefined,
             duration: js.UndefOr[Int] = js.undefined,
             friendsOnly: js.UndefOr[Boolean] = js.undefined,
             invitationOnly: js.UndefOr[Boolean] = js.undefined,
             levelCapAllowed: js.UndefOr[Boolean] = js.undefined,
             levelCap: js.UndefOr[Int] = js.undefined,
             perksAllowed: js.UndefOr[Boolean] = js.undefined,
             robotsAllowed: js.UndefOr[Boolean] = js.undefined): ContestCreationRequest = {
      new ContestCreationRequest(
        contestID = contestID ?? request.contestID,
        name = name ?? request.name,
        userID = userID ?? request.userID,
        startingBalance = startingBalance  ?? request.startingBalance,
        startAutomatically = startAutomatically ?? request.startAutomatically,
        duration = duration ?? request.duration,
        friendsOnly = friendsOnly ?? request.friendsOnly,
        invitationOnly = invitationOnly ?? request.invitationOnly,
        levelCapAllowed = levelCapAllowed ?? request.levelCapAllowed,
        levelCap = levelCap ?? request.levelCap,
        perksAllowed = perksAllowed ?? request.perksAllowed,
        robotsAllowed = robotsAllowed ?? request.robotsAllowed
      )
    }

    @inline
    def validate: js.Array[String] = {
      val messages = emptyArray[String]
      if (!request.name.exists(_.nonBlank)) messages.push("The game name is required")
      if (request.userID.isEmpty || request.userID.exists(_.isEmpty)) messages.push("The creator information is missing")
      if (request.levelCapAllowed.isTrue && request.levelCap.isEmpty) messages.push("Level cap must be specified")
      if (request.duration.flat.isEmpty) messages.push("The game duration is required")
      if (request.startingBalance.flat.isEmpty) messages.push("The starting balance is required")
      messages
    }

  }

}