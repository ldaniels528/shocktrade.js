package com.shocktrade.webapp.routes.contest.dao

import java.util.UUID

import com.shocktrade.common.forms.{ContestCreationForm, ContestCreationResponse, ContestSearchForm}
import com.shocktrade.common.models.contest.{ChatMessage, ContestRanking, ContestSearchResult, MyContest}
import com.shocktrade.server.dao.MySQLDAO
import io.scalajs.nodejs.console
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

  override def create(portfolio: PortfolioData)(implicit ec: ExecutionContext): Future[Int] = {
    import portfolio._
    conn.executeFuture(
      """|INSERT INTO portfolios (portfolioID, contestID, userID, funds) VALUES (?, ?, ?, ?)
         |""".stripMargin,
      js.Array(portfolioID.getOrElse(UUID.randomUUID().toString), contestID, userID, funds)) map (_.affectedRows)
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
         |	CR.contestID, CR.name, CR.hostUserID, CR.status,
         |	CR.userID playerID, CR.username AS playerName, CR.gainLoss playerGainLoss,
         |	LP.leaderID, LP.leaderName, LP.leaderGainLoss,
         |  CR.*
         |FROM contest_rankings CR
         |LEFT JOIN (
         |	SELECT contestID, userID AS leaderID, username AS leaderName, gainLoss AS leaderGainLoss
         |	FROM contest_rankings CR2
         |	WHERE totalEquity = (SELECT MAX(totalEquity) FROM contest_rankings WHERE contestID = CR2.contestID)
         |  LIMIT 1
         |) LP ON LP.contestID = CR.contestID
         |WHERE CR.userID = ?
         |""".stripMargin, js.Array(userID)).map(_._1)
  }

  override def findRankings(contestID: String)(implicit ec: ExecutionContext): Future[js.Array[ContestRanking]] = {
    conn.queryFuture[ContestRanking]("SELECT * FROM contest_rankings WHERE contestID = ?", js.Array(contestID))
      .map { case (rows, _) => rows }
  }

  override def search(form: ContestSearchForm)(implicit ec: ExecutionContext): Future[js.Array[ContestSearchResult]] = {
    var sql: String = null
    try {
      var options: List[String] = Nil
      form.buyIn.flat.foreach(bi => options = s"C.startingBalance <= ${bi.value}" :: options)
      form.continuousTrading.flat.foreach(checked => if (checked) options = "" :: options)
      form.duration.flat.foreach(gd => options = s"C.expirationTime > DATE_ADD(now(), INTERVAL ${gd.value} DAY)" :: options)
      form.friendsOnly.flat.foreach(checked => if (checked) options = "C.friendsOnly = 1" :: options)
      form.invitationOnly.flat.foreach(checked => if (checked) options = "C.invitationOnly = 1" :: options)
      form.nameLike.flat.foreach(name => options = s"C.name LIKE '%$name%'" :: options)
      form.perksAllowed.flat.foreach(checked => if (checked) options = "C.perksAllowed = 1" :: options)
      form.robotsAllowed.flat.foreach(checked => if (checked) options = "C.robotsAllowed = 1" :: options)
      form.status.flat.map(_.statusID).foreach {
        case 1 => options = "C.closedTime IS NULL" :: options // Active and Queued
        case 2 => options = "CS.status = 'ACTIVE'" :: options // Active Only
        case 3 => options = "CS.status = 'QUEUED'" :: options // Queued Only
        case _ =>
      }
      for (allowed <- form.levelCapAllowed; level <- form.levelCap) if (allowed) options = s"(levelCap = 0 OR levelCap < $level)" :: options
      val userID = form.userID.getOrElse("")
      sql =
        s"""|SELECT C.*, CS.status, HU.username AS hostUsername,
            |   DATEDIFF(C.expirationTime, C.startTime) AS duration,
            |	  IFNULL(PC.playerCount, 0) AS playerCount,
            |   IFNULL(PC.isParticipant, 0) isParticipant,
            |	  CASE WHEN hostUserID = ? THEN 1 ELSE 0 END AS isOwner
            |FROM contests C
            |INNER JOIN users HU ON HU.userID = C.hostUserID
            |INNER JOIN contest_statuses CS ON CS.statusID = C.statusID
            |LEFT JOIN (
            |   SELECT contestID, COUNT(*) AS playerCount,
            |	  SUM(CASE WHEN userID = ? THEN 1 ELSE 0 END) AS isParticipant
            |   FROM portfolios GROUP BY contestID
            |) AS PC ON PC.contestID = C.contestID
            |${if (options.nonEmpty) s"WHERE ${options.mkString(" AND ")} " else ""}
            |""".stripMargin
      conn.queryFuture[ContestSearchResult](sql, js.Array(userID, userID)).map(_._1)
    } catch {
      case e: Throwable =>
        if (sql != null) console.error(sql)
        throw js.JavaScriptException(e)
    }
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

  override def join(contestID: String, userID: String)(implicit ec: ExecutionContext): Future[Int] = {

    def deductFee(userID: String, startingBalance: js.UndefOr[Double]): Future[Int] = {
      conn.executeFuture("UPDATE users SET wallet = wallet - ? WHERE userID = ? AND wallet >= ?",
        js.Array(startingBalance, userID, startingBalance)).map(_.affectedRows) map {
        case count if count == 1 => count
        case count => throw js.JavaScriptException(s"Wallet could not be updated: count = $count")
      }
    }

    for {
      contest <- findOneByID(contestID) map {
        case Some(contest) => contest
        case None => throw js.JavaScriptException(s"Contest $contestID not found")
      }
      _ <- deductFee(userID, contest.startingBalance)
      w <- create(new PortfolioData(contestID = contestID, userID = userID, funds = contest.startingBalance))
    } yield w
  }

  override def quit(contestID: String, userID: String)(implicit ec: ExecutionContext): Future[Int] = {
    conn.executeFuture("CALL quitContest(?, ?)", js.Array(contestID, userID)).map(_.affectedRows)
  }

}