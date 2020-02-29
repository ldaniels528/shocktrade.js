package com.shocktrade.ingestion.daemons.nasdaq

import com.shocktrade.server.dao.DataAccessObjectHelper
import io.scalajs.npm.mysql.MySQLConnectionOptions

import scala.concurrent.{ExecutionContext, Future}

/**
 * NASDAQ Company Update DAO (supports AMEX, NASDAQ and NYSE)
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
trait NASDAQCompanyListUpdateDAO {

  def updateCompanyList(data: NASDAQCompanyData)(implicit ec: ExecutionContext): Future[Int]

}

/**
 * NASDAQ Company Update DAO Companion
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
object NASDAQCompanyListUpdateDAO {

  /**
   * Creates a new NASDAQ Company Update DAO instance
   * @param options the given [[MySQLConnectionOptions]]
   * @return a new [[NASDAQCompanyListUpdateDAO]]
   */
  def apply(options: MySQLConnectionOptions = DataAccessObjectHelper.getConnectionOptions): NASDAQCompanyListUpdateDAO =
    new NASDAQCompanyListUpdateDAOMySQL(options)

}
