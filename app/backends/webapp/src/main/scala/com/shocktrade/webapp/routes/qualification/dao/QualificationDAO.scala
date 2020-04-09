package com.shocktrade.webapp.routes.qualification.dao

import com.shocktrade.common.models.contest.ContestRef
import com.shocktrade.server.dao.DataAccessObjectHelper
import com.shocktrade.webapp.routes.contest.dao.{OrderData, PositionData}
import io.scalajs.npm.mysql.MySQLConnectionOptions

import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.js

/**
 * Qualification DAO
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
trait QualificationDAO {

  def closeExpiredContests(): Future[js.Array[ContestRef]]

  def createPosition(position: PositionData): Future[Int]

  def findQualifiedBuyOrders(limit: Int): Future[js.Array[QualifiedOrderData]]

  def findQualifiedSellOrders(limit: Int): Future[js.Array[QualifiedOrderData]]

  def updateOrder(order: OrderData): Future[Int]

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
