package com.shocktrade.webapp.routes.contest.dao

import com.shocktrade.common.forms.ContestSearchOptions
import com.shocktrade.common.forms.ContestSearchOptions._
import com.shocktrade.common.models.contest.{ChatMessage, ContestRanking, ContestSearchResult}
import com.shocktrade.server.common.LoggerFactory
import com.shocktrade.server.dao.MySQLDAO
import io.scalajs.npm.mysql.MySQLConnectionOptions
import io.scalajs.util.JsUnderOrHelper._

import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.js

/**
 * Contest DAO (MySQL implementation)
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
class ContestDAOMySQL(options: MySQLConnectionOptions)(implicit ec: ExecutionContext) extends MySQLDAO(options) with ContestDAO {
  private val logger = LoggerFactory.getLogger(getClass)

  override def findOneByID(contestID: String): Future[Option[ContestData]] = {
    conn.queryFuture[ContestData](
      """|SELECT C.*, CS.status
         |FROM contests C
         |LEFT JOIN contest_statuses CS ON CS.statusID = C.statusID
         |WHERE C.contestID = ?
         |""".stripMargin, js.Array(contestID))
      .map { case (rows, _) => rows.headOption }
  }

  override def findRankings(contestID: String): Future[js.Array[ContestRanking]] = {
    conn.queryFuture[ContestRanking]("SELECT * FROM contest_rankings WHERE contestID = ?", js.Array(contestID))
      .map { case (rows, _) => rows }
  }

  override def contestSearch(form: ContestSearchOptions): Future[js.Array[ContestSearchResult]] = {
    val sql = makeSearchSQL(form)
    try conn.queryFuture[ContestSearchResult](sql).map(_._1) catch {
      case e: Throwable =>
        logger.error(sql)
        throw js.JavaScriptException(e)
    }
  }

  private def makeSearchSQL(form: ContestSearchOptions): String = {
    var options: List[String] = Nil

    // build the search options
    form.buyIn.flat.foreach(bi => options = s"C.startingBalance <= ${bi.value}" :: options)
    form.continuousTrading.flat.foreach(checked => if (checked) options = "" :: options)
    form.duration.flat.foreach(gd => options = s"C.expirationTime > DATE_ADD(now(), INTERVAL ${gd.value} DAY)" :: options)
    form.friendsOnly.flat.foreach(checked => if (checked) options = "C.friendsOnly = 1" :: options)
    form.invitationOnly.flat.foreach(checked => if (checked) options = "C.invitationOnly = 1" :: options)
    for (allowed <- form.levelCapAllowed; level <- form.levelCap) if (allowed) options = s"(levelCap = 0 OR levelCap < $level)" :: options
    form.nameLike.flat.foreach(name => options = s"C.name LIKE '%$name%'" :: options)
    form.perksAllowed.flat.foreach(checked => if (checked) options = "C.perksAllowed = 1" :: options)
    form.robotsAllowed.flat.foreach(checked => if (checked) options = "C.robotsAllowed = 1" :: options)
    form.status.flat.map(_.statusID).foreach {
      case ACTIVE_AND_QUEUED => options = "C.closedTime IS NULL" :: options
      case ACTIVE_ONLY => options = "CS.status = 'ACTIVE'" :: options
      case QUEUED_ONLY => options = "CS.status = 'QUEUED'" :: options
      case _ =>
    }

    // include user-related search options?
    form.userID foreach { _ =>
      form.myGamesOnly.flat.foreach(checked => if (checked) options = s"(C.hostUserID = U.userID OR P.userID = U.userID)" :: options)
    }

    form.userID.toOption match {
      case Some(userID) => makeSearchSQLWithUser(userID, options)
      case None => makeSearchSQLWithoutUser(options)
    }
  }

  private def makeSearchSQLWithUser(userID: String, options: List[String]): String = {
    s"""|SELECT C.*, CS.status, HU.username AS hostUsername,
        |   DATEDIFF(C.expirationTime, C.startTime) AS duration,
        |	  IFNULL(PC.playerCount, 0) AS playerCount,
        |   IFNULL(PC.isParticipant, 0) isParticipant,
        |	  CASE WHEN hostUserID = '$userID' THEN 1 ELSE 0 END AS isOwner
        |FROM contests C
        |INNER JOIN contest_statuses CS ON CS.statusID = C.statusID
        |INNER JOIN users HU ON HU.userID = C.hostUserID
        |INNER JOIN users U ON U.userID = '$userID'
        |LEFT JOIN portfolios P ON P.contestID = C.contestID AND P.userID = U.userID
        |LEFT JOIN (
        |   SELECT contestID, COUNT(*) AS playerCount,
        |	  SUM(CASE WHEN userID = '$userID' THEN 1 ELSE 0 END) AS isParticipant
        |   FROM portfolios GROUP BY contestID
        |) AS PC ON PC.contestID = C.contestID
        |${if (options.nonEmpty) s"WHERE ${options.mkString(" AND ")} " else ""}
        |""".stripMargin
  }

  private def makeSearchSQLWithoutUser(options: List[String]): String = {
    """|SELECT
       |   C.*, CS.status, HU.username AS hostUsername,
       |   DATEDIFF(C.expirationTime, C.startTime) AS duration,
       |	 IFNULL(PC.playerCount, 0) AS playerCount,
       |   IFNULL(PC.isParticipant, 0) isParticipant,
       |	 0 AS isOwner
       |FROM contests C
       |INNER JOIN contest_statuses CS ON CS.statusID = C.statusID
       |INNER JOIN users HU ON HU.userID = C.hostUserID
       |LEFT JOIN (
       |   SELECT contestID, COUNT(*) AS playerCount, 0 AS isParticipant
       |   FROM portfolios GROUP BY contestID
       |) AS PC ON PC.contestID = C.contestID
       |""".stripMargin
  }

  override def findChatMessages(contestID: String): Future[js.Array[ChatMessage]] = {
    conn.queryFuture[ChatMessage](
      """|SELECT CC.*, U.username
         |FROM messages CC
         |INNER JOIN users U ON U.userID = CC.userID
         |WHERE CC.contestID = ?
         |ORDER BY CC.creationTime ASC
         |""".stripMargin,
      js.Array(contestID)) map { case (rows, _) => rows }
  }

}