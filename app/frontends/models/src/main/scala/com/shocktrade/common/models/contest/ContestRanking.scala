package com.shocktrade.common.models.contest

import com.shocktrade.common.util.StringHelper._
import io.scalajs.util.JsUnderOrHelper._

import scala.scalajs.js
import scala.scalajs.js.Date
import scala.scalajs.js.JSConverters._

/**
 * Contest Portfolio Ranking
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
class ContestRanking(val contestID: js.UndefOr[String],
                     val name: js.UndefOr[String],
                     val hostUserID: js.UndefOr[String],
                     val userID: js.UndefOr[String],
                     val totalXP: js.UndefOr[Int],
                     val portfolioID: js.UndefOr[String],
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

  def apply(contestID: js.UndefOr[String] = js.undefined,
            name: js.UndefOr[String] = js.undefined,
            hostUserID: js.UndefOr[String] = js.undefined,
            userID: js.UndefOr[String] = js.undefined,
            totalXP: js.UndefOr[Int] = js.undefined,
            portfolioID: js.UndefOr[String] = js.undefined,
            username: js.UndefOr[String] = js.undefined,
            startTime: js.UndefOr[js.Date] = js.undefined,
            expirationTime: js.UndefOr[js.Date] = js.undefined,
            startingBalance: js.UndefOr[Double] = js.undefined,
            status: js.UndefOr[String] = js.undefined,
            // indicators
            friendsOnly: js.UndefOr[Boolean] = js.undefined,
            invitationOnly: js.UndefOr[Boolean] = js.undefined,
            levelCap: js.UndefOr[String] = js.undefined,
            perksAllowed: js.UndefOr[Boolean] = js.undefined,
            robotsAllowed: js.UndefOr[Boolean] = js.undefined,
            // gain/loss/ranking
            rank: js.UndefOr[String] = js.undefined,
            rankNum: js.UndefOr[Int] = js.undefined,
            totalEquity: js.UndefOr[Double] = js.undefined,
            gainLoss: js.UndefOr[Double] = js.undefined): ContestRanking = {
    new ContestRanking(
      contestID = contestID,
      name = name,
      hostUserID = hostUserID,
      userID = userID,
      totalXP = totalXP,
      portfolioID = portfolioID,
      username = username,
      startTime = startTime,
      expirationTime = expirationTime,
      startingBalance = startingBalance,
      status = status,
      // indicators
      friendsOnly = friendsOnly,
      invitationOnly = invitationOnly,
      levelCap = levelCap,
      perksAllowed = perksAllowed,
      robotsAllowed = robotsAllowed,
      // gain/loss/ranking
      rank = rank,
      rankNum = rankNum,
      totalEquity = totalEquity,
      gainLoss = gainLoss
    )
  }

  def computeRankings(rankings: Seq[ContestRanking]): js.Array[ContestRanking] = {
    case class Accumulator(rankings: List[ContestRanking] = Nil, lastRanking: Option[ContestRanking] = None, index: Int = 1)

    // sort the rankings and add the position (e.g. "1st")
    val results = rankings.sortBy(r => (-r.totalEquity.orZero, -r.totalEquity.orZero)).foldLeft[Accumulator](Accumulator()) {
      case (acc@Accumulator(rankings, lastRanking, index), ranking) =>
        val newIndex = if (lastRanking.exists(_.totalEquity.exists(_ > ranking.totalEquity.orZero))) index + 1 else index
        val newRanking = ranking.copy(rank = newIndex.nth, rankNum = newIndex)
        acc.copy(rankings = newRanking :: rankings, lastRanking = Some(ranking), index = newIndex)
    }
    results.rankings.toJSArray
  }

  /**
   * Contest Ranking Enriched
   * @param ranking the given [[ContestRanking]]
   */
  final implicit class ContestRankingEnriched(val ranking: ContestRanking) extends AnyVal {

    def copy(contestID: js.UndefOr[String] = js.undefined,
             name: js.UndefOr[String] = js.undefined,
             hostUserID: js.UndefOr[String] = js.undefined,
             userID: js.UndefOr[String] = js.undefined,
             totalXP: js.UndefOr[Int] = js.undefined,
             portfolioID: js.UndefOr[String] = js.undefined,
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
        totalXP = totalXP ?? ranking.totalXP,
        portfolioID = portfolioID ?? ranking.portfolioID,
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