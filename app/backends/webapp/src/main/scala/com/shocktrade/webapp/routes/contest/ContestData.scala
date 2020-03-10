package com.shocktrade.webapp.routes.contest

import scala.scalajs.js

/**
 * Contest Data Model
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
class ContestData(var contestID: js.UndefOr[String],
                  var name: js.UndefOr[String],
                  var hostUserID: js.UndefOr[String],
                  var startTime: js.UndefOr[js.Date],
                  var expirationTime: js.UndefOr[js.Date],
                  var startingBalance: js.UndefOr[Double],
                  var status: js.UndefOr[String],
                  //var messages: js.UndefOr[js.Array[ChatMessage]],
                  // participants & rankings
                  //var participants: js.UndefOr[js.Array[Participant]],

                  // indicators
                  var friendsOnly: js.UndefOr[Boolean],
                  var invitationOnly: js.UndefOr[Boolean],
                  var levelCap: js.UndefOr[String],
                  var perksAllowed: js.UndefOr[Boolean],
                  var robotsAllowed: js.UndefOr[Boolean]) extends js.Object // ContestLike
