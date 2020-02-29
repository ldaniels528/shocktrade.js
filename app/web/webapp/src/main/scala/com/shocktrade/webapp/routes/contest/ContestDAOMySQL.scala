package com.shocktrade.webapp.routes.contest

import com.shocktrade.common.forms.ContestSearchForm
import com.shocktrade.server.common.LoggerFactory
import com.shocktrade.server.dao.MySQLDAO
import io.scalajs.npm.mysql.MySQLConnectionOptions

import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.js

/**
 * Contest DAO (MySQL implementation)
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
class ContestDAOMySQL(options: MySQLConnectionOptions) extends MySQLDAO(options) with ContestDAO {
  private val logger = LoggerFactory.getLogger(getClass)

  override def create(contest: ContestData)(implicit ec: ExecutionContext): Future[Int] = {
    import contest._
    conn.executeFuture(
      """|INSERT INTO contests (creationTime, contestID, name, startingBalance, startTime, expirationTime)
         |VALUES (now(), uuid(), ?, ?, ?, ?)
         |""".stripMargin,
      js.Array(name, startingBalance, startTime, expirationTime)) map (_.affectedRows)
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

  override def findOneByName(name: String)(implicit ec: ExecutionContext): Future[Option[ContestData]] = {
      conn.queryFuture[ContestData]("SELECT * FROM contests WHERE name = ?", js.Array(name))
        .map { case (rows, _) => rows.headOption }
  }

  override def findByUser(userID: String)(implicit ec: ExecutionContext): Future[js.Array[ContestRankingData]] = {
    conn.queryFuture[ContestRankingData](
      """|SELECT * FROM contest_rankings WHERE userID = ?
         |""".stripMargin,
      js.Array(userID)) map { case (rows, _) => rows }
  }

  override def findRankings(contestID: String)(implicit ec: ExecutionContext): Future[js.Array[ContestRankingData]] = {
    conn.queryFuture[ContestRankingData](
      """|SELECT * FROM contest_rankings WHERE contestID = ?
         |""".stripMargin,
      js.Array(contestID)) map { case (rows, _) => rows }
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
