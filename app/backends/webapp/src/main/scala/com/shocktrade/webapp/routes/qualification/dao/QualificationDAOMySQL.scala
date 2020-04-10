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

  override def closeOutExpiredContests(): Future[Int] = {
    conn.executeFuture(
      s"""|UPDATE contests C
          |INNER JOIN contest_statuses CS ON CS.status = 'CLOSED'
          |INNER JOIN order_price_types OPT ON OPT.name = 'MARKET_AT_CLOSE'
          |INNER JOIN portfolios P ON P.contestID = C.contestID
          |LEFT  JOIN positions PS ON PS.portfolioID = P.portfolioID
          |LEFT  JOIN stocks S ON S.symbol = PS.symbol AND S.exchange = PS.exchange
          |INNER JOIN users U ON U.userID = P.userID
          |SET
          |   C.closedTime = now(),
          |   C.statusID = CS.statusID,
          |   PS.active = 0,
          |   U.wallet = U.wallet + P.funds + IFNULL(S.lastTrade * PS.quantity, 0) - OPT.commission
          |WHERE closedTime IS NULL
          |AND expirationTime <= now()
          |""".stripMargin).map(_.affectedRows)
  }

  override def createPosition(position: PositionData): Future[Int] = {
    for {
      _ <- deductEntryFee(position)
      w2 <- insertPosition(position)
    } yield w2
  }

  override def findQualifiedBuyOrders(limit: Int): Future[js.Array[QualifiedOrderData]] = {
    conn.queryFuture[QualifiedOrderData](
      """|SELECT
         |	  P.contestID, P.userID, O.portfolioID, O.orderID,
         |	  S.symbol, S.`exchange`, S.volume, O.orderType, O.priceType,
         |    S.tradeDateTime, P.funds, O.quantity, S.lastTrade,
         |    S.lastTrade AS price,
         |    S.lastTrade * O.quantity + OPT.commission AS cost,
         |    O.creationTime,
         |    IFNULL(O.expirationTime, NOW()) AS expirationTime,
         |    OPT.commission
         |FROM orders O
         |INNER JOIN order_price_types OPT ON OPT.name = O.priceType
         |INNER JOIN portfolios P ON P.portfolioID = O.portfolioID
         |INNER JOIN stocks S ON S.symbol = O.symbol AND S.`exchange` = O.`exchange`
         |WHERE O.closed = O.fulfilled AND O.closed = 0
         |AND O.orderType = 'BUY'
         |AND S.tradeDateTime BETWEEN O.creationTime AND IFNULL(O.expirationTime, NOW())
         |AND S.volume > O.quantity
         |AND (
         |	(O.priceType = 'MARKET')
         |  OR (O.priceType = 'LIMIT' AND O.price >= S.lastTrade)
         |)
         |ORDER BY O.creationTime ASC
         |LIMIT ?
         |""".stripMargin, js.Array(limit)).map(_._1)
  }

  override def findQualifiedSellOrders(limit: Int): Future[js.Array[QualifiedOrderData]] = {
    conn.queryFuture[QualifiedOrderData](
      """|SELECT
         |	  P.contestID, P.userID, O.portfolioID, O.orderID,
         |	  S.symbol, S.`exchange`, S.volume, O.orderType, O.priceType,
         |    S.tradeDateTime, P.funds, O.quantity, S.lastTrade,
         |    S.lastTrade AS price,
         |    S.lastTrade * O.quantity + OPT.commission AS cost,
         |    O.creationTime,
         |    IFNULL(O.expirationTime, NOW()) AS expirationTime,
         |    OPT.commission
         |FROM orders O
         |INNER JOIN order_price_types OPT ON OPT.name = O.priceType
         |INNER JOIN portfolios P ON P.portfolioID = O.portfolioID
         |INNER JOIN stocks S ON S.symbol = O.symbol AND S.`exchange` = O.`exchange`
         |WHERE O.closed = O.fulfilled AND O.closed = 0
         |AND O.orderType = 'SELL'
         |AND S.tradeDateTime BETWEEN O.creationTime AND IFNULL(O.expirationTime, NOW())
         |AND S.volume > O.quantity
         |AND (
         |	(O.priceType = 'MARKET')
         |  OR (O.priceType = 'LIMIT' AND O.price <= S.lastTrade)
         |)
         |ORDER BY O.creationTime ASC
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
         |""".stripMargin, js.Array(closed, fulfilled, message, processedTime, orderID)).map(_.affectedRows) map {
      case count if count == 1 => count
      case count => throw js.JavaScriptException(s"Wallet could not be updated: count = $count")
    }
  }

  private def deductEntryFee(position: PositionData): Future[Int] = {
    import position._
    conn.executeFuture(
      """|UPDATE portfolios SET funds = funds - ?
         |WHERE portfolioID = ? AND funds >= ?
         |""".stripMargin, js.Array(cost, portfolioID, cost)).map(_.affectedRows) map {
      case count if count == 1 => count
      case count => throw js.JavaScriptException(s"Wallet could not be updated: count = $count")
    }
  }

  private def insertPosition(position: PositionData): Future[Int] = {
    import position._
    conn.executeFuture(
      """|INSERT INTO positions (
         |    positionID, portfolioID, orderID, symbol, exchange, price, quantity, netValue, cost, commission, tradeDateTime, processedTime
         |) VALUES (uuid(), ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
         |""".stripMargin, js.Array(portfolioID, orderID, symbol, exchange, price, quantity, netValue, cost, commission, tradeDateTime, processedTime)).map(_.affectedRows)
  }

}
