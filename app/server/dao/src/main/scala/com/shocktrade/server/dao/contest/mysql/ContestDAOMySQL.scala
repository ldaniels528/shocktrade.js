package com.shocktrade.server.dao.contest.mysql

import com.shocktrade.common.forms.ContestSearchForm
import com.shocktrade.server.common.LoggerFactory
import com.shocktrade.server.dao.MySQLDAO
import com.shocktrade.server.dao.contest.{ContestDAO, ContestData}
import io.scalajs.npm.mysql.MySQLConnectionOptions

import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.js

/**
 * MySQL implementation of the Awards DAO
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
class ContestDAOMySQL(options: MySQLConnectionOptions) extends MySQLDAO(options) with ContestDAO {
  private val logger = LoggerFactory.getLogger(getClass)

  override def create(contest: ContestData)(implicit ec: ExecutionContext): Future[Boolean] = {
    import contest._
    conn.executeFuture(
      """|INSERT INTO contests (contestID, name, startingBalance, startTime, expirationTime)
         |VALUES (?, ?, ?, ?, ?)
         |""".stripMargin,
      js.Array(contestID, name, startingBalance, startTime, expirationTime)) map (_.affectedRows > 0)
  }

  override def findActiveContests()(implicit ec: ExecutionContext): Future[js.Array[ContestData]] = {
    conn.queryFuture[ContestData](
      """|SELECT * FROM contests
         |WHERE expirationTime IS NULL OR expirationTime >= now()
         |""".stripMargin) map { case (rows, _) => rows }
  }

  override def findOneByID(contestID: String)(implicit ec: ExecutionContext): Future[Option[ContestData]] = {
    conn.queryFuture[ContestData]("SELECT * FROM contests WHERE contestID = ?", js.Array(contestID))
      .map { case (rows, _) => rows.headOption }
  }

  override def findByPlayer(playerID: String)(implicit ec: ExecutionContext): Future[js.Array[ContestData]] = {
    conn.queryFuture[ContestData](
      """|SELECT C.*
         |FROM contest_players P
         |INNER JOIN contests C ON C.contestID = P.contestID
         |WHERE P.playerID = ?
         |""".stripMargin,
      js.Array(playerID)) map { case (rows, _) => rows }
  }

  override def join(contestID: String, userID: String)(implicit ec: ExecutionContext): Future[Boolean] = {
    conn.executeFuture(
      s"""|INSERT INTO contest_players (contestID, userID, funds)
          |SELECT contestID, ?, startingBalance
          |FROM contests
          |WHERE contestID = ?
          |""".stripMargin,
      js.Array(userID, contestID)) map (_.affectedRows > 0)
  }

  override def search(form: ContestSearchForm)(implicit ec: ExecutionContext): Future[js.Array[ContestData]] = {
    var options: List[String] = Nil
    form.activeOnly.foreach(checked => if (checked) options = "status = 'Active'" :: options)
    form.friendsOnly.foreach(checked => if (checked) options = "friendsOnly = 1" :: options)
    form.perksAllowed.foreach(checked => if (checked) options = "perksAllowed = 1" :: options)
    form.invitationOnly.foreach(checked => if (checked) options = "invitationOnly = 1" :: options)
    form.perksAllowed.foreach(checked => if (checked) options = "perksAllowed = 1" :: options)
    form.robotsAllowed.foreach(checked => if (checked) options = "robotsAllowed = 1" :: options)
    for (allowed <- form.levelCapAllowed; level <- form.levelCap) if (allowed) options = s"(levelCap = 0 OR levelCap < $level)" :: options
    val sql = s"SELECT * FROM contests ${if (options.nonEmpty) s"WHERE ${options.mkString(" AND ")}" else ""}"
    logger.info(s"SQL: $sql")
    conn.queryFuture[ContestData](sql) map { case (rows, _) => rows }
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

  override def updateContests(contests: Seq[ContestData])(implicit ec: ExecutionContext): Future[Int] = {
    Future.sequence(contests.map(updateContest)).map(_.sum)
  }

}
