package com.shocktrade.client.models.contest

import com.shocktrade.common.models.contest.ContestRanking

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

/**
 * Contest Companion
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
object Contest {
  val MaxPlayers = 24

  // status constants
  val StatusActive = "ACTIVE"
  val StatusClosed = "CLOSED"

  /**
   * Contest Enrichment
   * @param contest the given [[ContestLike contest]]
   */
  implicit class ContestEnrichment(val contest: Contest) extends AnyVal {

    @inline
    def isActive: Boolean = contest.status.contains(StatusActive)

    @inline
    def isClosed: Boolean = contest.status.contains(StatusClosed)

    @inline
    def isEmpty: Boolean = contest.portfolios.exists(_.isEmpty)

    @inline
    def isFull: Boolean = contest.portfolios.exists(_.length >= MaxPlayers)

    @inline
    def isAlmostFull: Boolean = contest.portfolios.exists(_.length + 1 >= MaxPlayers)

  }

}