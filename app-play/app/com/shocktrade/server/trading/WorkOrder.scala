package com.shocktrade.server.trading

import java.util.Date

import com.shocktrade.models.contest.AccountTypes._
import com.shocktrade.models.contest.OrderTerms.OrderTerm
import com.shocktrade.models.contest.OrderTypes._
import com.shocktrade.models.contest.PriceTypes._
import com.shocktrade.models.contest.{Participant, ClosedOrder, MarginAccount}
import reactivemongo.bson.BSONObjectID

/**
 * Represents an ephemeral data transfer object for order claiming
 * @author lawrence.daniels@gmail.com
 */
case class WorkOrder(id: BSONObjectID,
                     participant: Participant,
                     symbol: String,
                     exchange: String,
                     orderTime: Date,
                     orderTerm: OrderTerm,
                     orderType: OrderType,
                     price: Option[BigDecimal],
                     priceType: PriceType,
                     quantity: Long,
                     commission: BigDecimal,
                     emailNotify: Boolean,
                     partialFulfillment: Boolean,
                     accountType: AccountType,
                     marginAccount: Option[MarginAccount]) {

  def expirationTime: Option[Date] = orderTerm.toDate(orderTime)

  def playerId: BSONObjectID = participant.id

  def toClosedOrder(asOfDate: Date, message: String) = ClosedOrder(
    id = id,
    accountType = accountType,
    symbol = symbol,
    exchange = exchange,
    creationTime = orderTime,
    orderTerm = orderTerm,
    processedTime = asOfDate,
    orderType = orderType,
    price = price getOrElse 0.00d,
    priceType = priceType,
    quantity = quantity,
    commission = commission,
    message = message
  )

}

