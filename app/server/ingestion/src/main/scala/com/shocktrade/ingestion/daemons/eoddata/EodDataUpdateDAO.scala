package com.shocktrade.ingestion.daemons.eoddata

import com.shocktrade.server.dao.DataAccessObjectHelper
import com.shocktrade.server.services.EodDataSecuritiesService.EodDataSecurity
import io.scalajs.npm.mysql.MySQLConnectionOptions

import scala.concurrent.{ExecutionContext, Future}

/**
 * EOD Data Update DAO
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
trait EodDataUpdateDAO {

  /**
   * Updates a stock with EOD Data
   * @param quote the given [[EodDataSecurity]]
   * @param ec the implicit [[ExecutionContext]]
   * @return the number of records updated
   */
  def update(quote: EodDataSecurity)(implicit ec: ExecutionContext): Future[Int]

  /**
   * Updates a collection of stocks with EOD Data
   * @param quotes the given collection of [[EodDataSecurity]]
   * @param ec the implicit [[ExecutionContext]]
   * @return the number of records updated
   */
  def updateAll(quotes: Seq[EodDataSecurity])(implicit ec: ExecutionContext): Future[Int] =
    Future.sequence(quotes.map(update)).map(_.sum)

}

/**
 * EOD Data Update DAO Companion
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
object EodDataUpdateDAO {

  /**
   * Creates a new EodData Update DAO instance
   * @param options the given [[MySQLConnectionOptions]]
   * @return a new [[EodDataUpdateDAO]]
   */
  def apply(options: MySQLConnectionOptions = DataAccessObjectHelper.getConnectionOptions): EodDataUpdateDAO = new EodDataUpdateDAOMySQL(options)

}