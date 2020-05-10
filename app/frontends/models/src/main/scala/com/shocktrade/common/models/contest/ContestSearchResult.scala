package com.shocktrade.common.models.contest

import com.shocktrade.common.AppConstants

import scala.scalajs.js

/**
 * Contest Search Result
 * @param contestID       the contest ID
 * @param name            the contest name
 * @param hostUserID      the contest host user ID
 * @param hostUsername    the contest host username
 * @param status          the contest status (e.g. "ACTIVE")
 * @param startTime       the contest start time
 * @param expirationTime  the contest expiration time
 * @param startingBalance the contest starting balance
 * @param timeOffset      the contest trading time offset
 * @param playerCount     the contest player count
 * @param friendsOnly     the contest friends-only indicator
 * @param invitationOnly  the contest invitation-only indicator
 * @param closed          indicates whether the contest has finished
 * @param levelCap        the optional contest level cap
 * @param perksAllowed    the contest perks-allowed indicator
 * @param robotsAllowed   the contest robots-allowed indicator
 * @param isOwner         indicates whether the current user is the contest owner
 * @param isParticipant   indicates whether the current user is a contest participant
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
class ContestSearchResult(val contestID: js.UndefOr[String],
                          val name: js.UndefOr[String],
                          val hostUserID: js.UndefOr[String],
                          val hostUsername: js.UndefOr[String],
                          val status: js.UndefOr[String],
                          val startTime: js.UndefOr[js.Date],
                          val expirationTime: js.UndefOr[js.Date],
                          val startingBalance: js.UndefOr[Double],
                          val timeOffset: js.UndefOr[Double],
                          val playerCount: js.UndefOr[Int],
                          val duration: js.UndefOr[Int],
                          val closed: js.UndefOr[Boolean],
                          // options
                          val friendsOnly: js.UndefOr[Boolean],
                          val invitationOnly: js.UndefOr[Boolean],
                          val levelCap: js.UndefOr[Int],
                          val perksAllowed: js.UndefOr[Boolean],
                          val robotsAllowed: js.UndefOr[Boolean],
                          // indicators
                          val isOwner: js.UndefOr[Boolean],
                          val isParticipant: js.UndefOr[Boolean],
                          // UI-related fields
                          var isExpanded: js.UndefOr[Boolean],
                          var isLoading: js.UndefOr[Boolean],
                          var rankings: js.UndefOr[js.Array[ContestRanking]]) extends ContestLike

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
    //  Contest Conditions
    ///////////////////////////////////////////////////////////////////////

    def isEmpty: Boolean = result.playerCount.contains(0)

    def isFull: Boolean = result.playerCount.exists(_ >= AppConstants.MaxPlayers)

    def isAlmostFull: Boolean = result.playerCount.exists(_ < AppConstants.MaxPlayers)

  }

}