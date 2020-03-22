package com.shocktrade.webapp.routes.contest.dao

import com.shocktrade.server.dao.DataAccessObjectHelper
import io.scalajs.npm.mysql.MySQLConnectionOptions

import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.js

/**
 * Order DAO
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
trait OrderDAO {

  def cancelOrder(orderID: String)(implicit ec: ExecutionContext): Future[Int]

  def createOrder(portfolioID: String, order: OrderData)(implicit ec: ExecutionContext): Future[Int]

  def findOrders(contestID: String, userID: String)(implicit ec: ExecutionContext): Future[js.Array[OrderData]]

}

/**
 * Order DAO Companion
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
object OrderDAO {

  /**
   * Creates a new Order DAO instance
   * @param options the given [[MySQLConnectionOptions]]
   * @return a new [[OrderDAO Order DAO]]
   */
  def apply(options: MySQLConnectionOptions = DataAccessObjectHelper.getConnectionOptions): OrderDAO = new OrderDAOMySQL(options)

}

