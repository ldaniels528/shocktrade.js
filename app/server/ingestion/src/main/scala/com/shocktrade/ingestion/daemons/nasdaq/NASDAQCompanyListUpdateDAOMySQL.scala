package com.shocktrade.ingestion.daemons.nasdaq

import com.shocktrade.server.dao.MySQLDAO
import io.scalajs.npm.mysql.MySQLConnectionOptions

import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.js

/**
 * NASDAQ Company Update DAO (MySQL implementation)
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
class NASDAQCompanyListUpdateDAOMySQL(options: MySQLConnectionOptions) extends MySQLDAO(options) with NASDAQCompanyListUpdateDAO {

  override def updateCompanyList(data: NASDAQCompanyData)(implicit ec: ExecutionContext): Future[Int] = {
    import data._
    conn.executeFuture(
      """|REPLACE INTO stocks_nasdaq (symbol, exchange, companyName, lastTrade, marketCap, ADRTSO, IPOyear, sector, industry, summary, quote)
         |VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
         |""".stripMargin,
      js.Array(symbol, exchange, companyName, lastTrade, marketCap, ADRTSO, IPOyear, sector, industry, summary, quote)) map (_.affectedRows)
  }

}
