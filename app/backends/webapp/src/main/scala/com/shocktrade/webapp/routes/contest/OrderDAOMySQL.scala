package com.shocktrade.webapp.routes.contest

import com.shocktrade.server.dao.MySQLDAO
import io.scalajs.npm.mysql.MySQLConnectionOptions

import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.js

/**
 * Order DAO (MySQL implementation)
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
class OrderDAOMySQL(options: MySQLConnectionOptions) extends MySQLDAO(options) with OrderDAO {

  override def cancelOrder(orderID: String)(implicit ec: ExecutionContext): Future[Int] = {
    conn.executeFuture(
      """|UPDATE orders
         |SET closed = 1, processedTime = now(), statusMessage = ?
         |WHERE orderID = ?
         |AND close = 0
         |""".stripMargin,
      js.Array("Canceled", orderID)) map (_.affectedRows)
  }

  override def createOrder(portfolioID: String, order: OrderData)(implicit ec: ExecutionContext): Future[Int] = {
    import order._
    conn.executeFuture(
      """|INSERT INTO orders (orderID, portfolioID, symbol, exchange, orderType, priceType, price, quantity)
         |VALUES (uuid(), ?, ?, ?, ?, ?, ?, ?)
         |""".stripMargin,
      js.Array(portfolioID, symbol, exchange, orderType, priceType, price, quantity)) map (_.affectedRows)
  }

  override def findOrders(contestID: String, userID: String)(implicit ec: ExecutionContext): Future[js.Array[OrderData]] = {
    conn.queryFuture[OrderData](
      """|SELECT O.*
         |FROM orders O
         |INNER JOIN portfolios P ON P.portfolioID = O.portfolioID
         |WHERE P.contestID = ? AND P.userID = ?
         |""".stripMargin,
      js.Array(contestID, userID)) map { case (rows, _) => rows }
  }

}
