package com.shocktrade.client.contest.models

import com.shocktrade.common.AppConstants
import com.shocktrade.common.models.contest.{ContestLike, ContestRanking}

import scala.scalajs.js

/**
 * Represents a Contest model
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
class Contest(var contestID: js.UndefOr[String] = js.undefined,
              var name: js.UndefOr[String] = js.undefined,
              var hostUserID: js.UndefOr[String] = js.undefined,
              var startTime: js.UndefOr[js.Date] = js.undefined,
              var expirationTime: js.UndefOr[js.Date] = js.undefined,
              var startingBalance: js.UndefOr[Double] = js.undefined,
              var status: js.UndefOr[String] = js.undefined,
              var timeOffset: js.UndefOr[Double] = js.undefined,
              // portfolios & rankings
              var portfolios: js.UndefOr[js.Array[Portfolio]] = js.undefined,
              var rankings: js.UndefOr[js.Array[ContestRanking]] = js.undefined,
              // indicators
              var friendsOnly: js.UndefOr[Boolean] = js.undefined,
              var invitationOnly: js.UndefOr[Boolean] = js.undefined,
              var levelCap: js.UndefOr[Int] = js.undefined,
              var perksAllowed: js.UndefOr[Boolean] = js.undefined,
              var robotsAllowed: js.UndefOr[Boolean] = js.undefined) extends ContestLike

/**
 * Contest Companion
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
object Contest {

  /**
   * Contest Enrichment
   * @param contest the given [[Contest]]
   */
  implicit class ContestEnrichment(val contest: Contest) extends AnyVal {

    @inline
    def isEmpty: Boolean = contest.portfolios.exists(_.isEmpty)

    @inline
    def isFull: Boolean = contest.portfolios.exists(_.length >= AppConstants.MaxPlayers)

    @inline
    def isAlmostFull: Boolean = contest.portfolios.exists(_.length + 1 >= AppConstants.MaxPlayers)

  }

}