package com.shocktrade.common.models.contest

import scala.scalajs.js

/**
 * Contest Search Result
 * @param contestID      the contest ID
 * @param name           the contest name
 * @param hostUserID     the contest host user ID
 * @param status         the contest status (e.g. "ACTIVE")
 * @param friendsOnly    the contest friends-only indicator
 * @param invitationOnly the contest invitation-only indicator
 * @param levelCap       the optional contest level cap
 * @param perksAllowed   the contest perks-allowed indicator
 * @param robotsAllowed  the contest robots-allowed indicator
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
class ContestSearchResult(val contestID: js.UndefOr[String],
                          val name: js.UndefOr[String],
                          val hostUserID: js.UndefOr[String],
                          val status: js.UndefOr[String],
                          val startTime: js.UndefOr[js.Date],
                          val expirationTime: js.UndefOr[js.Date],
                          val startingBalance: js.UndefOr[Double],
                          val playerCount: js.UndefOr[Int],
                          val closed: js.UndefOr[Boolean],
                          // indicators
                          val friendsOnly: js.UndefOr[Boolean],
                          val invitationOnly: js.UndefOr[Boolean],
                          val levelCap: js.UndefOr[String],
                          val perksAllowed: js.UndefOr[Boolean],
                          val robotsAllowed: js.UndefOr[Boolean]) extends js.Object

/**
 * ContestSearchResult Companion
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
object ContestSearchResult {

  /**
   * Contest Search Result Enriched
   * @param result the given [[ContestSearchResult]]
   */
  final implicit class ContestSearchResultEnriched(val result: ContestSearchResult) extends AnyVal {

    import io.scalajs.util.JsUnderOrHelper._

    ///////////////////////////////////////////////////////////////////////
    //  Contest Statuses
    ///////////////////////////////////////////////////////////////////////

    def isActive: Boolean = result.status.contains("ACTIVE")

    def isClosed: Boolean = result.status.contains("CLOSED")

    def isQQueued: Boolean = result.status.contains("QUEUED")

    ///////////////////////////////////////////////////////////////////////
    //  Contest Conditions
    ///////////////////////////////////////////////////////////////////////

    def isEmpty: Boolean = result.playerCount.contains(0)

    def isFull: Boolean = result.playerCount.exists(_ >= 24)

    def isAlmostFull: Boolean = result.playerCount.exists(_ < 24)

  }

}