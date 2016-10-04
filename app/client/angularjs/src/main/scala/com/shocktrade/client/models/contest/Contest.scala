package com.shocktrade.client.models.contest

import com.shocktrade.common.models.PlayerRef
import com.shocktrade.common.models.contest.{ChatMessage, ContestLike, ContestRankings, Participant}

import scala.scalajs.js
import scala.scalajs.js.annotation.ScalaJSDefined

/**
  * Represents a Contest model
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
@ScalaJSDefined
class Contest(var _id: js.UndefOr[String] = js.undefined,
              var name: js.UndefOr[String] = js.undefined,
              var creator: js.UndefOr[PlayerRef] = js.undefined,
              var startTime: js.UndefOr[js.Date] = js.undefined,
              var startingBalance: js.UndefOr[Double] = js.undefined,
              var status: js.UndefOr[String] = js.undefined,

              // collections
              var participants: js.UndefOr[js.Array[Participant]] = js.undefined,
              var messages: js.UndefOr[js.Array[ChatMessage]] = js.undefined,

              // UI-specific elements
              var loading: js.UndefOr[Boolean] = js.undefined,

              // indicators
              var friendsOnly: js.UndefOr[Boolean] = js.undefined,
              var invitationOnly: js.UndefOr[Boolean] = js.undefined,
              var levelCap: js.UndefOr[String] = js.undefined,
              var perksAllowed: js.UndefOr[Boolean] = js.undefined,
              var robotsAllowed: js.UndefOr[Boolean] = js.undefined) extends ContestLike {

  // UI-specific properties
  var rankings: js.UndefOr[ContestRankings] = js.undefined

  // administrative fields
  var error: js.UndefOr[String] = js.undefined
  var rankingsHidden: js.UndefOr[Boolean] = js.undefined
  var deleting: Boolean = false
  var joining: Boolean = false
  var quitting: Boolean = false
  var starting: Boolean = false

}