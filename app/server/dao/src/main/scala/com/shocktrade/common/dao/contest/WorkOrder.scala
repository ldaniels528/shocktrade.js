package com.shocktrade.common.dao.contest

import java.util.UUID

import com.shocktrade.Commissions
import com.shocktrade.common.dao.Claim
import org.scalajs.nodejs.mongodb.ObjectID

import scala.scalajs.js
import scala.scalajs.js.annotation.ScalaJSDefined

/**
  * Work Order
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
@ScalaJSDefined
class WorkOrder(val portfolioID: ObjectID, val order: OrderData, val claim: Claim) extends js.Object

/**
  * Work Order Companion
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
object WorkOrder {

  /**
    * Work Order Extensions
    * @param wo the given [[WorkOrder work order]]
    */
  implicit class WorkOrderExtensions(val wo: WorkOrder) extends AnyVal {

    @inline
    def toClosedOrder(statusMessage: String) = wo.order.copy(
      processedTime = wo.claim.asOfTime,
      statusMessage = statusMessage
    )

    @inline
    def toNewPosition = new PositionData(
      _id = UUID.randomUUID().toString,
      accountType = wo.order.accountType,
      symbol = wo.order.symbol,
      exchange = wo.claim.exchange,
      pricePaid = wo.claim.price,
      quantity = wo.claim.quantity,
      commission = Commissions(wo.order),
      netValue = for {totalCost <- wo.order.totalCost} yield totalCost - Commissions(wo.order),
      processedTime = new js.Date()
    )

    @inline
    def totalCost = wo.order.totalCost getOrElse 0d

  }

}