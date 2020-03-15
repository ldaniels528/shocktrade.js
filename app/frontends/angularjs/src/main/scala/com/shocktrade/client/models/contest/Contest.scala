package com.shocktrade.client.models.contest

import com.shocktrade.common.models.contest.{ChatMessage, ContestRanking}

import scala.scalajs.js

/**
 * Represents a Contest model
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
class Contest(var contestID: js.UndefOr[String] = js.undefined,
              var name: js.UndefOr[String] = js.undefined,
              var hostUserID: js.UndefOr[String] = js.undefined,
              var startTime: js.UndefOr[js.Date] = js.undefined,
              var startingBalance: js.UndefOr[Double] = js.undefined,
              var status: js.UndefOr[String] = js.undefined,
              // chats
              var messages: js.UndefOr[js.Array[ChatMessage]] = js.undefined,
              // portfolios & rankings
              var portfolios: js.UndefOr[js.Array[Portfolio]] = js.undefined,
              var rankings: js.UndefOr[js.Array[ContestRanking]] = js.undefined,
              // indicators
              var friendsOnly: js.UndefOr[Boolean] = js.undefined,
              var invitationOnly: js.UndefOr[Boolean] = js.undefined,
              var levelCap: js.UndefOr[Int] = js.undefined,
              var perksAllowed: js.UndefOr[Boolean] = js.undefined,
              var robotsAllowed: js.UndefOr[Boolean] = js.undefined) extends js.Object {

  // administrative fields
  var error: js.UndefOr[String] = js.undefined
  var loading: js.UndefOr[Boolean] = js.undefined
  var rankingsHidden: js.UndefOr[Boolean] = js.undefined
  var deleting: Boolean = false
  var joining: Boolean = false
  var quitting: Boolean = false
  var starting: Boolean = false

}
