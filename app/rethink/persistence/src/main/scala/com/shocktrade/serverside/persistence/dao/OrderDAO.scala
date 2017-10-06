package com.shocktrade.serverside.persistence.dao

import com.shocktrade.serverside.persistence.dao.mysql.OrderDAOMySQL

import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.js

/**
  * Order DAO
  * @author lawrence.daniels@gmail.com
  */
trait OrderDAO {

  /**
    * Closes an existing order
    * @param order the given [[OrderData order]]
    * @return a promise of the closure result
    */
  def closeOrder(order: OrderData): Future[Boolean]

  /**
    * Creates a new order
    * @param order the given [[OrderData order]]
    * @return a promise of the creation result
    */
  def createOrder(order: OrderData): Future[Boolean]

  /**
    * Processes open BUY and SELL orders
    * @param effectiveTime the given [[js.Date effective time]]
    * @return a promise of the number of orders processed
    */
  def processOrders(effectiveTime: js.Date): Future[Int]

  /**
    * Updates an existing order
    * @param order the given [[OrderData order]]
    * @return a promise of the update result
    */
  def updateOrder(order: OrderData): Future[Boolean]

}

/**
  * Order DAO Companion
  * @author lawrence.daniels@gmail.com
  */
object OrderDAO {

  def apply()(implicit ec: ExecutionContext): OrderDAO = new OrderDAOMySQL()

}
