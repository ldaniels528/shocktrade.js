package com.shocktrade.webapp.routes.contest.dao

import java.util.UUID

import com.shocktrade.common.forms.ContestSearchForm._
import com.shocktrade.common.forms.{ContestCreationRequest, ContestCreationResponse, ContestSearchForm}
import com.shocktrade.common.models.contest.{ChatMessage, ContestRanking, ContestSearchResult}
import com.shocktrade.server.common.LoggerFactory
import com.shocktrade.server.dao.MySQLDAO
import io.scalajs.npm.mysql.MySQLConnectionOptions
import io.scalajs.util.JsUnderOrHelper._

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.js

/**
 * Contest DAO (MySQL implementation)
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
class ContestDAOMySQL(options: MySQLConnectionOptions)(implicit ec: ExecutionContext) extends MySQLDAO(options) with ContestDAO {
  private val logger = LoggerFactory.getLogger(getClass)

  override def create(request: ContestCreationRequest): Future[ContestCreationResponse] = {
    // define the contestID, portfolioID and required parameters
    val (contestID, portfolioID) = (UUID.randomUUID().toString, UUID.randomUUID().toString)
    val params = for {
      name <- request.name.flat.toOption
      userID <- request.userID.flat.toOption
      duration <- request.duration.flat.toOption
      startingBalance <- request.startingBalance.flat.toOption
    } yield (name, userID, startingBalance, duration)

    // create the contest
    params match {
      case Some((name, userID, startingBalance, duration)) =>
        for {
          _ <- deductFee(userID, startingBalance) map checkInsertCount
          _ <- insertContest(contestID, userID, name, startingBalance, duration)(request) map checkInsertCount
          _ <- insertPortfolio(contestID, portfolioID, userID, startingBalance) map checkInsertCount
          _ <- putChatMessage(contestID, userID, message = s"Welcome to $name!") map checkInsertCount
        } yield new ContestCreationResponse(contestID, portfolioID)
      case None =>
        Future.failed(js.JavaScriptException("userID and startingBalance are required"))
    }
  }

  override def create(portfolio: PortfolioData): Future[Int] = {
    import portfolio._
    conn.executeFuture(
      "INSERT INTO portfolios (portfolioID, contestID, userID, funds) VALUES (?, ?, ?, ?)",
      js.Array(portfolioID.getOrElse(UUID.randomUUID().toString), contestID, userID, funds)) map (_.affectedRows)
  }

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

  override def contestSearch(form: ContestSearchForm): Future[js.Array[ContestSearchResult]] = {
    val sql = makeSearchSQL(form)
    try conn.queryFuture[ContestSearchResult](sql).map(_._1) catch {
      case e: Throwable =>
        logger.error(sql)
        throw js.JavaScriptException(e)
    }
  }

  private def makeSearchSQL(form: ContestSearchForm): String = {
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

  override def start(contestID: String, userID: String): Future[Boolean] = {
    conn.executeFuture(
      s"""|UPDATE portfolios P
          |INNER JOIN contest_statuses CS ON CS.status = 'ACTIVE'
          |SET startTime = now(), statusID = CS.statusID
          |WHERE contestID = ? AND hostUserID = ? AND statusID <> CS.statusID
          |""".stripMargin,
      js.Array(contestID, userID)) map (_.affectedRows > 0)
  }

  override def updateContests(contests: Seq[ContestData]): Future[Int] = {
    Future.sequence(contests.map(updateContest)).map(_.sum)
  }

  override def updateContest(contest: ContestData): Future[Int] = {
    import contest._
    conn.executeFuture("UPDATE contests SET name = ? WHERE contestID = ?", js.Array(name, contestID)).map(_.affectedRows)
  }

  override def putChatMessage(contestID: String, userID: String, message: String): Future[Int] = {
    conn.executeFuture(
      """|INSERT INTO contest_chats (messageID, contestID, userID, message)
         |VALUES (uuid(), ?, ?, ?)
         |""".stripMargin,
      js.Array(contestID, userID, message)) map (_.affectedRows)
  }

  override def findChatMessages(contestID: String): Future[js.Array[ChatMessage]] = {
    conn.queryFuture[ChatMessage](
      """|SELECT CC.*, U.username
         |FROM contest_chats CC
         |INNER JOIN users U ON U.userID = CC.userID
         |WHERE CC.contestID = ?
         |ORDER BY CC.creationTime ASC
         |""".stripMargin,
      js.Array(contestID)) map { case (rows, _) => rows }
  }

  override def join(contestID: String, userID: String): Future[Int] = {
    for {
      contest <- findOneByID(contestID) map {
        case Some(contest) => contest
        case None => throw js.JavaScriptException(s"Contest $contestID not found")
      }
      startingBalance = contest.startingBalance.getOrElse(throw js.JavaScriptException("startingBalance is required"))
      _ <- deductFee(userID, startingBalance)
      w <- create(new PortfolioData(contestID = contestID, userID = userID, funds = contest.startingBalance))
    } yield w
  }

  override def quit(contestID: String, userID: String): Future[Int] = {
    conn.executeFuture("CALL quitContest(?, ?)", js.Array(contestID, userID)).map(_.affectedRows)
  }

  private def deductFee(userID: String, startingBalance: Double): Future[Int] = {
    conn.executeFuture("UPDATE users SET wallet = wallet - ? WHERE userID = ? AND wallet >= ?",
      js.Array(startingBalance, userID, startingBalance)).map(_.affectedRows) map checkUpdateCount
  }

  private def insertContest(contestID: String, userID: String, name: String, startingBalance: Double, duration: Int)(request: ContestCreationRequest): Future[Int] = {
    conn.executeFuture(
      """|INSERT INTO contests (
         |  contestID, hostUserID, name, statusID, startingBalance, expirationTime,
         |  friendsOnly, invitationOnly, levelCap, perksAllowed, robotsAllowed
         |)
         |SELECT ?, ?, ?, CS.statusID, ?, ?, ?, ?, ?, ?, ?
         |FROM contest_statuses CS
         |WHERE CS.status = 'ACTIVE'
         |""".stripMargin,
      js.Array(contestID, userID, name, startingBalance,
        new js.Date(js.Date.now() + duration * 1.day.toMillis), request.friendsOnly ?? false, request.invitationOnly ?? false,
        request.levelCap ?? 0, request.perksAllowed ?? true, request.robotsAllowed ?? true)).map(_.affectedRows)
  }

  private def insertPortfolio(contestID: String, portfolioID: String, userID: String, startingBalance: Double): Future[Int] = {
    conn.executeFuture("INSERT INTO portfolios (contestID, portfolioID, userID, funds) VALUES (?, ?, ?, ?)",
      js.Array(contestID, portfolioID, userID, startingBalance)).map(_.affectedRows)
  }

}