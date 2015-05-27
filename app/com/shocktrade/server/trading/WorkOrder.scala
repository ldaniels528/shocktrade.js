package com.shocktrade.server.trading

import java.util.Date

import com.shocktrade.models.contest.AccountTypes._
import com.shocktrade.models.contest.ClosedOrder
import com.shocktrade.models.contest.OrderTypes._
import com.shocktrade.models.contest.PriceTypes._
import reactivemongo.bson.BSONObjectID

/**
 * Represents an ephemeral data transfer object for order claiming
 * @author lawrence.daniels@gmail.com
 */
case class WorkOrder(id: BSONObjectID,
                     playerId: BSONObjectID,
                     symbol: String,
                     exchange: String,
                     orderTime: Date,
                     expirationTime: Option[Date],
                     orderType: OrderType,
                     price: Option[BigDecimal],
                     priceType: PriceType,
                     quantity: Long,
                     commission: BigDecimal,
                     emailNotify: Boolean,
                     partialFulfillment: Boolean,
                     accountType: AccountType) {

  def toClosedOrder(asOfDate: Date, message: String) = ClosedOrder(
    id = id,
    accountType = accountType,
    symbol = symbol,
    exchange = exchange,
    orderTime = orderTime,
    expirationTime = expirationTime,
    processedTime = asOfDate,
    orderType = orderType,
    price = price,
    priceType = priceType,
    quantity = quantity,
    commission = commission,
    message = message
  )

}

