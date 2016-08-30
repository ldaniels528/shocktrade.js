package com.shocktrade.server.data

import com.shocktrade.javascript.models.contest.{Order, Position}
import org.scalajs.nodejs.mongodb.ObjectID

import scala.scalajs.js
import scala.scalajs.js.annotation.ScalaJSDefined

/**
  * Work Order
  * @author lawrence.daniels@gmail.com
  */
@ScalaJSDefined
class WorkOrder(val portfolioID: ObjectID, val order: Order, val claim: Claim) extends js.Object

/**
  * Work Order Companion
  * @author lawrence.daniels@gmail.com
  */
object WorkOrder {

  /**
    * Work Order Extensions
    * @param wo the given [[WorkOrder work order]]
    */
  implicit class WorkOrderExtensions(val wo: WorkOrder) extends AnyVal {

    @inline
    def toClosedOrder(statusMessage: String) = wo.order.copy(
      creationTime = wo.claim.asOfTime,
      statusMessage = statusMessage
    )

    @inline
    def toNewPosition = new Position(
      accountType = wo.order.accountType,
      symbol = wo.order.symbol,
      exchange = wo.claim.exchange,
      pricePaid = wo.claim.price,
      quantity = wo.claim.quantity,
      commission = wo.order.commission,
      netValue = wo.totalCost,
      processedTime = js.Date.now()
    )

    @inline
    def totalCost = wo.claim.price * wo.claim.quantity + wo.order.commission.getOrElse(0.00)

  }

}