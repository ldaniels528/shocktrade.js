package com.shocktrade.serverside.persistence.dao.mysql

import com.shocktrade.serverside.persistence.dao.{OrderDAO, OrderData}
import io.scalajs.npm.moment.Moment
import io.scalajs.npm.mysql.{MySQLConnectionOptions, MySQL}

import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.js

/**
  * MySQL implementation of the Order DAO
  * @author lawrence.daniels@gmail.com
  */
class OrderDAOMySQL()(implicit ec: ExecutionContext) extends OrderDAO {
  private val datePattern = "YYYY-MM-DD HH:mm:ss"
  private val conn = MySQL.createConnection(new MySQLConnectionOptions(
    host = "dev001",
    database = "shocktrade",
    user = "webapp",
    password = "shock1"
  ))

  override def closeOrder(order: OrderData): Future[Boolean] = {
    val sql =
      s"""
         |UPDATE orders
         |SET
         |  closed = 1,
         |  processedTime = now(),
         |  message = '${order.message.getOrElse("")}'
         |WHERE orderID = '${order.orderID}'
         |AND closed = 0
       """.stripMargin
    conn.executeFuture(sql) map (_.affectedRows > 0)
  }

  override def createOrder(order: OrderData): Future[Boolean] = {
    import order._
    val effectiveTime = Moment(order.effectiveTime).format(datePattern)
    val expirationTime = order.expirationTime.map(d => Moment(d).format(datePattern))
    val sql =
      s"""
         |INSERT INTO orders (orderID, userID, symbol, exchange, orderType, price, priceType, quantity, creationTime, expirationTime)
         |VALUES (
         |  '$orderID', '$userID', '$symbol', '$exchange', '$orderType', ${price.getOrElse(0d)}, '$priceType',
         |  $quantity, '$effectiveTime', '$expirationTime'
         |)
         |""".stripMargin
    conn.executeFuture(sql) map (_.affectedRows > 0)
  }

  override def processOrders(effectiveTime: js.Date): Future[Int] = {
    val effTime = Moment(effectiveTime).format(datePattern)
    val sql = s"CALL createPositions('$effTime')"
    conn.executeFuture(sql) map (_.affectedRows)
  }

  override def updateOrder(order: OrderData): Future[Boolean] = {
    import order._
    val effectiveTime = Moment(order.effectiveTime).format(datePattern)
    val expirationTime = order.expirationTime.map(d => Moment(d).format(datePattern))
    val sql =
      s"""
         |UPDATE orders
         |SET
         |  userID = '$userID',
         |  price = ${price.getOrElse(0d)},
         |  priceType = '$priceType',
         |  creationTime = '$effectiveTime',
         |  expirationTime = '$expirationTime'
         |WHERE orderID = '$orderID'
         |""".stripMargin
    conn.executeFuture(sql) map (_.affectedRows > 0)
  }

}
