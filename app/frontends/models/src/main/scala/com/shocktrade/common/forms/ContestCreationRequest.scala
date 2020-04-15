package com.shocktrade.common.forms

import com.shocktrade.common.util.StringHelper._
import io.scalajs.util.JsUnderOrHelper._
import io.scalajs.util.ScalaJsHelper._

import scala.scalajs.js

/**
 * Contest Creation Request
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
class ContestCreationRequest(val name: js.UndefOr[String] = js.undefined,
                             val userID: js.UndefOr[String] = js.undefined,
                             val startingBalance: js.UndefOr[Double] = js.undefined,
                             val startAutomatically: js.UndefOr[Boolean] = js.undefined,
                             val duration: js.UndefOr[Int] = js.undefined,
                             val friendsOnly: js.UndefOr[Boolean] = js.undefined,
                             val invitationOnly: js.UndefOr[Boolean] = js.undefined,
                             val levelCapAllowed: js.UndefOr[Boolean] = js.undefined,
                             val levelCap: js.UndefOr[Int] = js.undefined,
                             val perksAllowed: js.UndefOr[Boolean] = js.undefined,
                             val robotsAllowed: js.UndefOr[Boolean] = js.undefined) extends js.Object

/**
 * Contest Creation Request
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
object ContestCreationRequest {
  
  /**
   * Contest Creation Request Extensions
   * @param request the given [[ContestCreationRequest request]]
   */
  implicit class ContestCreationRequestExtensions(val request: ContestCreationRequest) extends AnyVal {

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