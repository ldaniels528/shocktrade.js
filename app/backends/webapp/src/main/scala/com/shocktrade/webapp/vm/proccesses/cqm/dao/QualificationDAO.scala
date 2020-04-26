package com.shocktrade.webapp.vm.proccesses.cqm.dao

import com.shocktrade.server.dao.DataAccessObjectHelper
import io.scalajs.npm.mysql.MySQLConnectionOptions

import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.js

/**
 * Qualification DAO
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
trait QualificationDAO {

  def findExpiredContests(): Future[js.Array[ContestExpiredData]]

  def findExpiredOrders(): Future[js.Array[OrderExpiredData]]

  def findLimitAndMarketOrders(limit: Int): Future[js.Array[QualifiedOrderData]]

  def findMarketCloseOrders(limit: Int): Future[js.Array[QualifiedOrderData]]

}

/**
 * Qualification DAO Companion
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
object QualificationDAO {

  /**
   * Creates a new Qualification DAO instance
   * @param options the given [[MySQLConnectionOptions]]
   * @return a new [[QualificationDAO Qualification DAO]]
   */
  def apply(options: MySQLConnectionOptions = DataAccessObjectHelper.getConnectionOptions)(implicit ec: ExecutionContext): QualificationDAO = new QualificationDAOMySQL(options)

}
