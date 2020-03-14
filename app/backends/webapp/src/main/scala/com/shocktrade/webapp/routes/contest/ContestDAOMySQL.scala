package com.shocktrade.webapp.routes.contest

import com.shocktrade.common.forms.{ContestCreationForm, ContestCreationResponse, ContestSearchForm}
import com.shocktrade.common.models.contest.{ContestRanking, ContestSearchResult, MyContest}
import com.shocktrade.server.dao.MySQLDAO
import io.scalajs.npm.mysql.MySQLConnectionOptions
import io.scalajs.util.JsUnderOrHelper._

import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.js

/**
 * Contest DAO (MySQL implementation)
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
class ContestDAOMySQL(options: MySQLConnectionOptions) extends MySQLDAO(options) with ContestDAO {

  override def create(form: ContestCreationForm)(implicit ec: ExecutionContext): Future[Option[ContestCreationResponse]] = {
    import form._
    conn.queryFuture[ContestCreationResponse](
      "CALL createContest(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)", js.Array(
        name, userID, startingBalance.flatMap(_.value), startAutomatically, duration.flatMap(_.value),
        friendsOnly ?? false, invitationOnly ?? false, levelCap.flatMap(_.value) ?? 0, perksAllowed ?? true, robotsAllowed ?? true
      ).map(_.orNull)
    ) map { case (rows, _) => rows.headOption }
  }

  override def findOneByID(contestID: String)(implicit ec: ExecutionContext): Future[Option[ContestData]] = {
    conn.queryFuture[ContestData](
      """|SELECT C.*, CS.status
         |FROM contests C
         |LEFT JOIN contest_statuses CS ON CS.statusID = C.statusID
         |WHERE C.contestID = ?
         |""".stripMargin, js.Array(contestID))
      .map { case (rows, _) => rows.headOption }
  }

  override def findMyContests(userID: String)(implicit ec: ExecutionContext): Future[js.Array[MyContest]] = {
    conn.queryFuture[MyContest](
      """|SELECT
         |  C.contestID, C.name, C.hostUserID, CS.status, C.*,
         |  P.playerID, P.playerName, P.playerGainLoss,
         |  L.leaderID, L.leaderName, L.leaderGainLoss
         |FROM contests C
         |LEFT JOIN contest_statuses CS ON CS.statusID = C.statusID
         |LEFT JOIN (
         |	SELECT contestID, userID AS leaderID, username AS leaderName, gainLoss AS leaderGainLoss
         |	FROM contest_rankings D
         |	WHERE gainLoss = (SELECT MAX(gainLoss) FROM contest_rankings WHERE contestID = D.contestID AND hostUserID = D.hostUserID)
         |	LIMIT 1
         |) L ON L.contestID = C.contestID
         |LEFT JOIN (
         |	SELECT contestID, userID AS playerID, username AS playerName, gainLoss AS playerGainLoss
         |	FROM contest_rankings
         |	WHERE userID = ?
         |	LIMIT 1
         |) P ON P.contestID = C.contestID
         |WHERE C.hostUserID = ?
         |AND P.playerID IS NOT NULL
         |""".stripMargin, js.Array(userID, userID))
      .map { case (rows, _) => rows }
  }

  override def findRankings(contestID: String)(implicit ec: ExecutionContext): Future[js.Array[ContestRanking]] = {
    conn.queryFuture[ContestRanking]("SELECT * FROM contest_rankings WHERE contestID = ?", js.Array(contestID))
      .map { case (rows, _) => rows }
  }

  override def join(contestID: String, userID: String)(implicit ec: ExecutionContext): Future[Boolean] = {
    conn.executeFuture(
      s"""|INSERT INTO portfolios (contestID, userID, funds)
          |SELECT contestID, ?, startingBalance
          |FROM contests
          |WHERE contestID = ?
          |""".stripMargin,
      js.Array(userID, contestID)) map (_.affectedRows > 0)
  }

  override def search(form: ContestSearchForm)(implicit ec: ExecutionContext): Future[js.Array[ContestSearchResult]] = {
    var options: List[String] = Nil
    form.activeOnly.foreach(checked => if (checked) options = "(now() BETWEEN startTime and expirationTime OR expirationTime IS NULL)" :: options)
    form.friendsOnly.foreach(checked => if (checked) options = "friendsOnly = 1" :: options)
    form.perksAllowed.foreach(checked => if (checked) options = "perksAllowed = 1" :: options)
    form.invitationOnly.foreach(checked => if (checked) options = "invitationOnly = 1" :: options)
    form.perksAllowed.foreach(checked => if (checked) options = "perksAllowed = 1" :: options)
    form.robotsAllowed.foreach(checked => if (checked) options = "robotsAllowed = 1" :: options)
    for (allowed <- form.levelCapAllowed; level <- form.levelCap) if (allowed) options = s"(levelCap = 0 OR levelCap < $level)" :: options
    val sql =
      s"""|SELECT C.*, CS.status, IFNULL(P.playerCount, 0) AS playerCount
          |FROM contests C
          |LEFT JOIN contest_statuses CS ON CS.statusID = C.statusID
          |LEFT JOIN (
          |   SELECT contestID, COUNT(*) AS playerCount FROM portfolios GROUP BY contestID
          |) AS P ON P.contestID = C.contestID
          |${if (options.nonEmpty) s"WHERE ${options.mkString(" AND ")} " else ""}
          |""".stripMargin
    conn.queryFuture[ContestSearchResult](sql) map { case (rows, _) => rows }
  }

  override def updateContests(contests: Seq[ContestData])(implicit ec: ExecutionContext): Future[Int] = {
    Future.sequence(contests.map(updateContest)).map(_.sum)
  }

  override def updateContest(contest: ContestData)(implicit ec: ExecutionContext): Future[Int] = {
    import contest._
    conn.executeFuture(
      """|UPDATE contests
         |SET name = ?
         |WHERE contestID = ?
         |""".stripMargin,
      js.Array(name, contestID)) map (_.affectedRows)
  }

}