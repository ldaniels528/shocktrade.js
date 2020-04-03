package com.shocktrade.common.models.contest

import scala.scalajs.js

/**
 * Represents a My Contest model
 * @param contestID      the contest ID
 * @param name           the contest name
 * @param hostUserID     the contest host user ID
 * @param status         the contest status (e.g. "ACTIVE")
 * @param playerID       the contest player ID
 * @param playerName     the contest player name
 * @param playerGainLoss the contest player gain/loss
 * @param playerRank     the contest player ranking (e.g. "1st")
 * @param leaderID       the contest leader ID
 * @param leaderName     the contest leader name
 * @param leaderGainLoss the contest leader gain/loss
 * @param friendsOnly    the contest friends-only indicator
 * @param invitationOnly the contest invitation-only indicator
 * @param levelCap       the optional contest level cap
 * @param perksAllowed   the contest perks-allowed indicator
 * @param robotsAllowed  the contest robots-allowed indicator
 */
class MyContest(val contestID: js.UndefOr[String] = js.undefined,
                val name: js.UndefOr[String] = js.undefined,
                val hostUserID: js.UndefOr[String] = js.undefined,
                val status: js.UndefOr[String] = js.undefined,
                val startTime: js.UndefOr[js.Date] = js.undefined,
                val expirationTime: js.UndefOr[js.Date] = js.undefined,
                val timeOffset: js.UndefOr[Double] = js.undefined,
                val playerCount: js.UndefOr[Int] = js.undefined,
                // player
                val playerID: js.UndefOr[String] = js.undefined,
                val playerName: js.UndefOr[String] = js.undefined,
                val playerGainLoss: js.UndefOr[Double] = js.undefined,
                val playerRank: js.UndefOr[Int] = js.undefined,
                // leader
                val leaderID: js.UndefOr[String] = js.undefined,
                val leaderName: js.UndefOr[String] = js.undefined,
                val leaderGainLoss: js.UndefOr[Double] = js.undefined,
                val leaderRank: js.UndefOr[Int] = js.undefined,
                // indicators
                val friendsOnly: js.UndefOr[Boolean] = js.undefined,
                val invitationOnly: js.UndefOr[Boolean] = js.undefined,
                val levelCap: js.UndefOr[String] = js.undefined,
                val perksAllowed: js.UndefOr[Boolean] = js.undefined,
                val robotsAllowed: js.UndefOr[Boolean] = js.undefined) extends js.Object

/**
 * MyContest Companion
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
object MyContest {

  /**
   * MyContest Extensions
   * @param myContest the given [[MyContest]]
   */
  final implicit class MyContestExtensions(val myContest: MyContest) extends AnyVal {

    import io.scalajs.util.JsUnderOrHelper._

    def copy(contestID: js.UndefOr[String] = js.undefined,
             name: js.UndefOr[String] = js.undefined,
             hostUserID: js.UndefOr[String] = js.undefined,
             status: js.UndefOr[String] = js.undefined,
             startTime: js.UndefOr[js.Date] = js.undefined,
             expirationTime: js.UndefOr[js.Date] = js.undefined,
             timeOffset: js.UndefOr[Double] = js.undefined,
             playerCount: js.UndefOr[Int] = js.undefined,
             playerID: js.UndefOr[String] = js.undefined,
             playerName: js.UndefOr[String] = js.undefined,
             playerGainLoss: js.UndefOr[Double] = js.undefined,
             playerRank: js.UndefOr[Int] = js.undefined,
             leaderID: js.UndefOr[String] = js.undefined,
             leaderName: js.UndefOr[String] = js.undefined,
             leaderGainLoss: js.UndefOr[Double] = js.undefined,
             leaderRank: js.UndefOr[Int] = js.undefined,
             friendsOnly: js.UndefOr[Boolean] = js.undefined,
             invitationOnly: js.UndefOr[Boolean] = js.undefined,
             levelCap: js.UndefOr[String] = js.undefined,
             perksAllowed: js.UndefOr[Boolean] = js.undefined,
             robotsAllowed: js.UndefOr[Boolean] = js.undefined): MyContest = {
      new MyContest(contestID = contestID ?? myContest.contestID,
        name = name ?? myContest.name,
        hostUserID = hostUserID ?? myContest.hostUserID,
        status = status ?? myContest.status,
        startTime = startTime ?? myContest.startTime,
        expirationTime = expirationTime ?? myContest.expirationTime,
        timeOffset = timeOffset ?? myContest.timeOffset,
        playerCount = playerCount ?? myContest.playerCount,
        playerID = playerID ?? myContest.playerID,
        playerName = playerName ?? myContest.playerName,
        playerGainLoss = playerGainLoss ?? myContest.playerGainLoss,
        playerRank = playerRank ?? myContest.playerRank,
        leaderID = leaderID ?? myContest.leaderID,
        leaderName = leaderName ?? myContest.leaderName,
        leaderGainLoss = leaderGainLoss ?? myContest.leaderGainLoss,
        leaderRank = leaderRank ?? myContest.leaderRank,
        friendsOnly = friendsOnly ?? myContest.friendsOnly,
        invitationOnly = invitationOnly ?? myContest.invitationOnly,
        levelCap = levelCap ?? myContest.levelCap,
        perksAllowed = perksAllowed ?? myContest.perksAllowed,
        robotsAllowed = robotsAllowed ?? myContest.robotsAllowed)
    }
  }

}
