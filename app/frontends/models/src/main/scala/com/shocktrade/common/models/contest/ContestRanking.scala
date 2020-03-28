package com.shocktrade.common.models.contest

import scala.scalajs.js
import scala.scalajs.js.Date

/**
 * Contest Portfolio Ranking
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
class ContestRanking(val contestID: js.UndefOr[String],
                     val name: js.UndefOr[String],
                     val hostUserID: js.UndefOr[String],
                     val userID: js.UndefOr[String],
                     val username: js.UndefOr[String],
                     val startTime: js.UndefOr[js.Date],
                     val expirationTime: js.UndefOr[js.Date],
                     val startingBalance: js.UndefOr[Double],
                     val status: js.UndefOr[String],
                     // indicators
                     val friendsOnly: js.UndefOr[Boolean],
                     val invitationOnly: js.UndefOr[Boolean],
                     val levelCap: js.UndefOr[String],
                     val perksAllowed: js.UndefOr[Boolean],
                     val robotsAllowed: js.UndefOr[Boolean],
                     // gain/loss/ranking
                     val rank: js.UndefOr[String],
                     val rankNum: js.UndefOr[Int],
                     val totalEquity: js.UndefOr[Double],
                     val gainLoss: js.UndefOr[Double]) extends js.Object

/**
 * Contest Portfolio Ranking Companion
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
object ContestRanking {

  import io.scalajs.util.JsUnderOrHelper._

  final implicit class ContestRankingEnriched(val ranking: ContestRanking) extends AnyVal {

    def copy(contestID: js.UndefOr[String] = js.undefined,
             name: js.UndefOr[String] = js.undefined,
             hostUserID: js.UndefOr[String] = js.undefined,
             userID: js.UndefOr[String] = js.undefined,
             username: js.UndefOr[String] = js.undefined,
             startTime: js.UndefOr[Date] = js.undefined,
             expirationTime: js.UndefOr[Date] = js.undefined,
             startingBalance: js.UndefOr[Double] = js.undefined,
             status: js.UndefOr[String] = js.undefined,
             friendsOnly: js.UndefOr[Boolean] = js.undefined,
             invitationOnly: js.UndefOr[Boolean] = js.undefined,
             levelCap: js.UndefOr[String] = js.undefined,
             perksAllowed: js.UndefOr[Boolean] = js.undefined,
             robotsAllowed: js.UndefOr[Boolean] = js.undefined,
             rank: js.UndefOr[String] = js.undefined,
             rankNum: js.UndefOr[Int] = js.undefined,
             totalEquity: js.UndefOr[Double] = js.undefined,
             gainLoss: js.UndefOr[Double] = js.undefined): ContestRanking = {
      new ContestRanking(
        contestID = contestID ?? ranking.contestID,
        name = name ?? ranking.name,
        hostUserID = hostUserID ?? ranking.hostUserID,
        userID = userID ?? ranking.userID,
        username = username ?? ranking.username,
        startTime = startTime ?? ranking.startTime,
        expirationTime = expirationTime ?? ranking.expirationTime,
        startingBalance = startingBalance ?? ranking.startingBalance,
        status = status ?? ranking.status,
        friendsOnly = friendsOnly ?? ranking.friendsOnly,
        invitationOnly = invitationOnly ?? ranking.invitationOnly,
        levelCap = levelCap ?? ranking.levelCap,
        perksAllowed = perksAllowed ?? ranking.perksAllowed,
        robotsAllowed = robotsAllowed ?? ranking.robotsAllowed,
        rank = rank ?? ranking.rank,
        rankNum = rankNum ?? ranking.rankNum,
        totalEquity = totalEquity ?? ranking.totalEquity,
        gainLoss = gainLoss ?? ranking.gainLoss)
    }

  }

}