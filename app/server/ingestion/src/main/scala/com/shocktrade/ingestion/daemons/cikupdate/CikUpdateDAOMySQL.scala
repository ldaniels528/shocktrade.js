package com.shocktrade.ingestion.daemons.cikupdate

import com.shocktrade.server.dao.MySQLDAO
import io.scalajs.npm.mysql.MySQLConnectionOptions

import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.js

/**
 * CIK Update DAO (MySQL implementation)
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
class CikUpdateDAOMySQL(options: MySQLConnectionOptions) extends MySQLDAO(options) with CikUpdateDAO {

  override def findMissing(implicit ec: ExecutionContext): Future[js.Array[MissingCikData]] = {
    conn.queryFuture[MissingCikData](
      """|SELECT A.symbol, A.exchange
         |FROM stocks A
         |LEFT JOIN stocks_cik B ON B.symbol = A.symbol AND B.exchange = A.exchange
         |WHERE B.symbol IS NULL
         |""".stripMargin
    ) map { case (rows, _) => rows }
  }

  override def updateCik(cik: CikUpdateData)(implicit ec: ExecutionContext): Future[Int] = {
    import cik._
    conn.executeFuture(
      """|REPLACE INTO stocks_cik (symbol, exchange, companyName, cikNumber, mailingAddress)
         |VALUES (?, ?, ?, ?, ?)
         |""".stripMargin,
      js.Array(symbol, exchange, companyName, cikNumber, mailingAddress)) map (_.affectedRows)
  }

}
