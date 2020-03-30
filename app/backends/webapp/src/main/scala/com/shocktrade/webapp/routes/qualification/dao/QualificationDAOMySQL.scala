package com.shocktrade.webapp.routes.qualification.dao

import com.shocktrade.server.dao.MySQLDAO
import com.shocktrade.webapp.routes.contest.dao.{OrderData, PositionData}
import io.scalajs.npm.mysql.MySQLConnectionOptions

import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.js

/**
 * Qualification DAO (MySQL implementation)
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
class QualificationDAOMySQL(options: MySQLConnectionOptions)(implicit ec: ExecutionContext) extends MySQLDAO(options) with QualificationDAO {

  override def createPosition(position: PositionData): Future[Int] = {
    import position._
    for {
      w1 <- conn.executeFuture(
        """|UPDATE portfolios SET funds = funds - ?
           |WHERE portfolioID = ? AND funds >= ?
           |""".stripMargin, js.Array(cost, portfolioID, cost)).map(_.affectedRows) if w1 == 1

      w2 <- conn.executeFuture(
        """|INSERT INTO positions (
           |    positionID, portfolioID, orderID, symbol, exchange, price, quantity, cost, commission, tradeDateTime, processedTime
           |) VALUES (uuid(), ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
           |""".stripMargin, js.Array(portfolioID, orderID, symbol, exchange, price, quantity, cost, commission, tradeDateTime, processedTime)).map(_.affectedRows)
    } yield w2
  }

  override def findQualifiedOrders(limit: Int): Future[js.Array[QualifiedOrderData]] = {
    conn.queryFuture[QualifiedOrderData](
      """|SELECT * FROM qualifications
         |ORDER BY creationTime ASC
         |LIMIT ?
         |""".stripMargin, js.Array(limit)).map(_._1)
  }

  override def updateOrder(order: OrderData): Future[Int] = {
    import order._
    conn.executeFuture(
      """|UPDATE orders
         |SET
         |  closed = ?,
         |  fulfilled = ?,
         |  message = ?,
         |  processedTime = ?
         |WHERE orderID = ?
         |""".stripMargin, js.Array(closed, fulfilled, message, processedTime, orderID)).map(_.affectedRows)
  }

}
