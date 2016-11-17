package com.shocktrade.server.dao.contest

import java.util.UUID

import com.shocktrade.common.Commissions
import org.scalajs.nodejs.mongodb.ObjectID
import org.scalajs.nodejs.util.ScalaJsHelper._
import org.scalajs.sjs.JsUnderOrHelper._

import scala.scalajs.js
import scala.scalajs.js.annotation.ScalaJSDefined

/**
  * Work Order
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
@ScalaJSDefined
class WorkOrder(val portfolioID: ObjectID,
                val order: OrderData,
                val symbol: String,
                val exchange: String,
                val price: Double,
                val quantity: Double,
                val asOfTime: js.Date) extends js.Object

/**
  * Work Order Companion
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
object WorkOrder {

  @inline
  def apply(portfolioID: ObjectID, order: OrderData, claim: Claim) = new WorkOrder(
    portfolioID,
    order,
    claim.symbol,
    claim.exchange,
    claim.price,
    claim.quantity,
    claim.asOfTime
  )

  /**
    * Work Order Extensions
    * @param wo the given [[WorkOrder work order]]
    */
  implicit class WorkOrderExtensions(val wo: WorkOrder) extends AnyVal {

    @inline
    def fundingSource = {
      wo.order match {
        case o if o.isCashAccount => "cashAccount.funds"
        case o if o.isMarginAccount => "marginAccount.funds"
        case o => die(s"Invalid account type (${wo.order.accountType.orNull}) for order # ${wo.order._id.orNull}")
      }
    }

    @inline
    def fundingAsOfDate = {
      wo.order match {
        case o if o.isCashAccount => "cashAccount.asOfDate"
        case o if o.isMarginAccount => "marginAccount.asOfDate"
        case o => die(s"Invalid account type (${wo.order.accountType.orNull}) for order # ${wo.order._id.orNull}")
      }
    }

    @inline
    def toClosedOrder(statusMessage: String) = wo.order.copy(
      processedTime = wo.asOfTime,
      statusMessage = statusMessage
    )

    @inline
    def toNewPosition = new PositionData(
      _id = UUID.randomUUID().toString,
      accountType = wo.order.accountType,
      symbol = wo.order.symbol,
      exchange = wo.exchange,
      pricePaid = wo.price,
      quantity = wo.quantity,
      commission = Commissions(wo.order),
      netValue = for {totalCost <- wo.order.totalCost} yield totalCost - Commissions(wo.order),
      processedTime = new js.Date()
    )

    @inline
    def toPerformance(positionSold: PositionData) = new PerformanceData(
      _id = UUID.randomUUID().toString,
      symbol = wo.order.symbol,
      pricePaid = positionSold.pricePaid,
      priceSold = wo.price,
      quantity = wo.quantity,
      commissions = positionSold.commission.map(_ + Commissions(wo.order))
    )

    @inline
    def totalCost = {
      (wo.order match {
        case o if o.isCashAccount => wo.order.totalCost
        case o if o.isMarginAccount => wo.order.totalCost.map(_ / 2.0)
        case o => die(s"Invalid account type (${wo.order.accountType.orNull}) for order # ${wo.order._id.orNull}")
      }) orDie "Total cost could not be determined"
    }

  }

}