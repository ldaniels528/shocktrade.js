package com.shocktrade.ingestion.daemons.cikupdate

import com.shocktrade.server.dao.DataAccessObjectHelper
import io.scalajs.npm.mysql.MySQLConnectionOptions

import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.js

/**
 * CIK Update DAO
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
trait CikUpdateDAO {

  def findMissing(implicit ec: ExecutionContext): Future[js.Array[MissingCikData]]

  def updateCik(cik: CikUpdateData)(implicit ec: ExecutionContext): Future[Int]

}

/**
 * CIK Update DAO Companion
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
object CikUpdateDAO {

  /**
   * Creates a new CIK Update DAO instance
   * @param options the given [[MySQLConnectionOptions]]
   * @return a new [[CikUpdateDAO]]
   */
  def apply(options: MySQLConnectionOptions = DataAccessObjectHelper.getConnectionOptions): CikUpdateDAO = new CikUpdateDAOMySQL(options)

}
