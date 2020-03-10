package com.shocktrade.client.models.contest

import com.shocktrade.common.models.contest.{ChatMessage, ContestLike, Participant}
import com.shocktrade.common.models.user.User

import scala.scalajs.js

/**
 * Represents a Contest model
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
class Contest(var contestID: js.UndefOr[String] = js.undefined,
              var name: js.UndefOr[String] = js.undefined,
              var creator: js.UndefOr[User] = js.undefined,
              var startTime: js.UndefOr[js.Date] = js.undefined,
              var startingBalance: js.UndefOr[Double] = js.undefined,
              var status: js.UndefOr[String] = js.undefined,
              var messages: js.UndefOr[js.Array[ChatMessage]] = js.undefined,
              // participants & rankings
              var participants: js.UndefOr[js.Array[Participant]] = js.undefined,
              var leader: js.UndefOr[Participant] = js.undefined,
              var player: js.UndefOr[Participant] = js.undefined,
              // UI-specific elements
              var loading: js.UndefOr[Boolean] = js.undefined,
              // indicators
              var friendsOnly: js.UndefOr[Boolean] = js.undefined,
              var invitationOnly: js.UndefOr[Boolean] = js.undefined,
              var levelCap: js.UndefOr[Int] = js.undefined,
              var perksAllowed: js.UndefOr[Boolean] = js.undefined,
              var robotsAllowed: js.UndefOr[Boolean] = js.undefined) extends ContestLike {

  // administrative fields
  var error: js.UndefOr[String] = js.undefined
  var rankingsHidden: js.UndefOr[Boolean] = js.undefined
  var deleting: Boolean = false
  var joining: Boolean = false
  var quitting: Boolean = false
  var starting: Boolean = false

}

/**
 * Contest Companion
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
object Contest {

  /**
   * Contest Enrichment
   * @param contest the given [[Contest contest]]
   */
  final implicit class ContestEnrichment(val contest: Contest) extends AnyVal {

    @inline
    def totalInvestment: js.UndefOr[Double] = contest.player.flatMap(_.totalEquity)

  }

}