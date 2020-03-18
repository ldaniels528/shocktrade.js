package com.shocktrade.ingestion.daemons.onetime

import com.shocktrade.server.dao.DataAccessObjectHelper
import io.scalajs.npm.mysql.{MySQL, MySQLConnection, MySQLConnectionOptions}

import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.js

/**
 * Wikipedia Company DAO
 * @param options the given [[MySQLConnectionOptions]]
 */
class WikipediaCompanyDAO(options: MySQLConnectionOptions) {
  private val conn: MySQLConnection = MySQL.createConnection(options)

  def close(): Unit = conn.destroy()

  def insert(data: WikipediaCompanyData)(implicit ec: ExecutionContext): Future[Int] = {
    import data._
    conn.executeFuture(
      """|REPLACE INTO stocks_wikipedia (symbol, name, sector, industry, cityState, initialReportingDate, cikNumber, yearFounded)
         |VALUES (?, ?, ?, ?, ?, ?, ?, ?)
         |""".stripMargin,
      js.Array(symbol, name, sector, industry, cityState, initialReportingDate, cikNumber, yearFounded)) map (_.affectedRows)
  }

}

/**
 * Wikipedia Company DAO
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
object WikipediaCompanyDAO {

  /**
   * Creates a new NASDAQ Company Update DAO instance
   * @param options the given [[MySQLConnectionOptions]]
   * @return a new [[WikipediaCompanyDAO]]
   */
  def apply(options: MySQLConnectionOptions = DataAccessObjectHelper.getConnectionOptions): WikipediaCompanyDAO = new WikipediaCompanyDAO(options)

}