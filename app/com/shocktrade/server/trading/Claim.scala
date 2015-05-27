package com.shocktrade.server.trading

import java.util.Date

import com.shocktrade.models.contest.{Performance, Position}

import scala.language.{implicitConversions, postfixOps}

/**
 * Represents an ephemeral claiming artifact, which will eventually become a new/updated position
 * @author lawrence.daniels@gmail.com
 */
case class Claim(symbol: String,
                 exchange: String,
                 price: BigDecimal,
                 quantity: Long,
                 commission: BigDecimal,
                 purchaseTime: Date,
                 workOrder: WorkOrder) {

  /**
   * Computes the cost of the order (BUY orders)
   */
  def cost: BigDecimal = price * quantity + commission

  /**
   * Computes the proceeds of the order (SELL orders)
   */
  def proceeds: BigDecimal = price * quantity - commission

  def toPerformance(existingPos: Position) = {
    // TODO include original order IDs?
    Performance(
      symbol = symbol,
      exchange = exchange,
      pricePaid = existingPos.pricePaid,
      priceSold = price,
      quantity = quantity,
      commissions = existingPos.commission + commission,
      purchasedDate = existingPos.processedTime,
      soldDate = purchaseTime
    )
  }

  def toPosition = Position(
    accountType = workOrder.accountType,
    symbol = symbol,
    exchange = exchange,
    pricePaid = price,
    quantity = quantity,
    commission = commission,
    processedTime = purchaseTime
  )

  def toPositionIncrease(existingPos: Position) = {
    val oldCost = existingPos.pricePaid * existingPos.quantity.toDouble
    val newCost = price * quantity.toDouble
    val totalCost = oldCost + newCost
    val totalQuantity = existingPos.quantity + quantity
    val newUnitPrice = totalCost / totalQuantity.toDouble

    // create the position
    Position(
      accountType = existingPos.accountType,
      symbol = symbol,
      exchange = exchange,
      pricePaid = newUnitPrice,
      quantity = totalQuantity,
      commission = commission,
      processedTime = purchaseTime
    )
  }

  def toPositionDecrease(existingPos: Position) = {
    // compute the adjusted quantity
    val reducedQuantity = existingPos.quantity - quantity

    // create the position
    Position(
      accountType = existingPos.accountType,
      symbol = symbol,
      exchange = exchange,
      pricePaid = existingPos.pricePaid,
      quantity = reducedQuantity,
      commission = commission,
      processedTime = purchaseTime
    )
  }

}