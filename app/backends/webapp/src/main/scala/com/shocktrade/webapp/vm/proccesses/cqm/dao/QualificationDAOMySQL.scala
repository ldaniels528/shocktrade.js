package com.shocktrade.webapp.vm.proccesses.cqm.dao

import com.shocktrade.server.dao.MySQLDAO
import io.scalajs.npm.mysql.MySQLConnectionOptions

import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.js

/**
 * Qualification DAO (MySQL implementation)
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
class QualificationDAOMySQL(options: MySQLConnectionOptions)(implicit ec: ExecutionContext)
  extends MySQLDAO(options) with QualificationDAO {

  override def findExpiredContests(): Future[js.Array[ContestExpiredData]] = {
    conn.queryFuture[ContestExpiredData](
      """|SELECT C.contestID, P.portfolioID, C.expirationTime
         |FROM contests C
         |INNER JOIN portfolios P ON P.contestID = C.contestID
         |WHERE C.closedTime IS NULL
         |AND C.expirationTime <= now()
         |""".stripMargin).map(_._1)
  }

  override def findExpiredOrders(): Future[js.Array[OrderExpiredData]] = {
    conn.queryFuture[OrderExpiredData](
      """|SELECT O.*
         |FROM orders O
         |WHERE O.closed = 0
         |AND O.expirationTime <= now()
         |""".stripMargin).map(_._1)
  }

  override def findQualifiedOrders(limit: Int): Future[js.Array[QualifiedOrderData]] = {
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
         |INNER JOIN contests C ON C.contestID = P.contestID
         |INNER JOIN stocks S ON S.symbol = O.symbol AND S.`exchange` = O.`exchange`
         |WHERE O.closed = O.fulfilled AND O.closed = 0
         |AND C.closedTime IS NULL
         |AND S.tradeDateTime BETWEEN O.creationTime AND IFNULL(O.expirationTime, DATE_ADD(NOW(), INTERVAL 1 DAY))
         |AND S.volume > O.quantity
         |AND (
         |  (O.orderType = 'BUY' AND ((O.priceType = 'MARKET') OR (O.priceType = 'LIMIT' AND O.price >= S.lastTrade)))
         |  OR (O.orderType = 'SELL' AND ((O.priceType = 'MARKET') OR (O.priceType = 'LIMIT' AND O.price <= S.lastTrade)))
         |)
         |ORDER BY O.creationTime ASC
         |LIMIT ?
         |""".stripMargin, js.Array(limit)).map(_._1)
  }

}
