package com.shocktrade.webapp.routes.contest

import com.shocktrade.common.forms.{ContestCreationForm, ContestCreationResponse, ContestSearchForm}
import com.shocktrade.common.models.contest.{ChatMessage, ContestRanking, ContestSearchResult, MyContest}
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
    conn.executeFuture("CALL joinContest(?, ?)", js.Array(contestID, userID)).map(_.affectedRows > 0)
  }

  override def quit(contestID: String, userID: String)(implicit ec: ExecutionContext): Future[Boolean] = {
    conn.executeFuture("CALL quitContest(?, ?)", js.Array(contestID, userID)).map(_.affectedRows > 0)
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
    val userID = form.userID.getOrElse("")
    val sql =
      s"""|SELECT C.*, CS.status,
          |	  IFNULL(PC.playerCount, 0) AS playerCount,
          |   IFNULL(PC.isParticipant, 0) isParticipant,
          |	  CASE WHEN hostUserID = ? THEN 1 ELSE 0 END AS isOwner
          |FROM contests C
          |LEFT JOIN contest_statuses CS ON CS.statusID = C.statusID
          |LEFT JOIN (
          |   SELECT contestID, COUNT(*) AS playerCount,
          |	  SUM(CASE WHEN userID = ? THEN 1 ELSE 0 END) AS isParticipant
          |   FROM portfolios GROUP BY contestID
          |) AS PC ON PC.contestID = C.contestID
          |${if (options.nonEmpty) s"WHERE ${options.mkString(" AND ")} " else ""}
          |""".stripMargin
    conn.queryFuture[ContestSearchResult](sql, js.Array(userID, userID)) map { case (rows, _) => rows }
  }

  override def start(contestID: String, userID: String)(implicit ec: ExecutionContext): Future[Boolean] = {
    conn.executeFuture(
      s"""|UPDATE portfolios P
          |INNER JOIN contest_statuses CS ON CS.status = 'ACTIVE'
          |SET startTime = now(), statusID = CS.statusID
          |WHERE contestID = ? AND hostUserID = ? AND statusID <> CS.statusID
          |""".stripMargin,
      js.Array(contestID, userID)) map (_.affectedRows > 0)
  }

  override def updateContests(contests: Seq[ContestData])(implicit ec: ExecutionContext): Future[Int] = {
    Future.sequence(contests.map(updateContest)).map(_.sum)
  }

  override def updateContest(contest: ContestData)(implicit ec: ExecutionContext): Future[Int] = {
    import contest._
    conn.executeFuture("UPDATE contests SET name = ? WHERE contestID = ?", js.Array(name, contestID)).map(_.affectedRows)
  }

  override def addChatMessage(contestID: String, userID: String, message: String)(implicit ec: ExecutionContext): Future[Int] = {
    conn.executeFuture(
      """|INSERT INTO contest_chats (messageID, contestID, userID, message)
         |VALUES (uuid(), ?, ?, ?)
         |""".stripMargin,
      js.Array(contestID, userID, message)) map (_.affectedRows)
  }

  override def findChatMessages(contestID: String)(implicit ec: ExecutionContext): Future[js.Array[ChatMessage]] = {
    conn.queryFuture[ChatMessage](
      """|SELECT CC.*, U.username
         |FROM contest_chats CC
         |INNER JOIN users U ON U.userID = CC.userID
         |WHERE CC.contestID = ?
         |ORDER BY CC.creationTime ASC
         |""".stripMargin,
      js.Array(contestID)) map { case (rows, _) => rows }
  }

}