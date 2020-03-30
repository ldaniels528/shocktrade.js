package com.shocktrade.webapp.routes.contest.dao

import java.util.UUID

import com.shocktrade.common.models.contest.PositionLike
import io.scalajs.util.JsUnderOrHelper._

import scala.scalajs.js

/**
 * Position Data
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
class PositionData(val positionID: js.UndefOr[String] = UUID.randomUUID().toString,
                   val portfolioID: js.UndefOr[String],
                   val orderID: js.UndefOr[String],
                   val symbol: js.UndefOr[String],
                   val exchange: js.UndefOr[String],
                   val price: js.UndefOr[Double],
                   val quantity: js.UndefOr[Double],
                   val tradeDateTime: js.UndefOr[js.Date],
                   val commission: js.UndefOr[Double],
                   val processedTime: js.UndefOr[js.Date],
                   val cost: js.UndefOr[Double],
                   val netValue: js.UndefOr[Double]) extends PositionLike

/**
 * Position Companion
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
object PositionData {

  /**
   * Position Enrichment
   * @param position the given [[PositionData position]]
   */
  final implicit class PositionDataEnrichment(val position: PositionData) extends AnyVal {

    @inline
    def copy(positionID: js.UndefOr[String] = js.undefined,
             portfolioID: js.UndefOr[String] = js.undefined,
             orderID: js.UndefOr[String] = js.undefined,
             symbol: js.UndefOr[String] = js.undefined,
             exchange: js.UndefOr[String] = js.undefined,
             pricePaid: js.UndefOr[Double] = js.undefined,
             quantity: js.UndefOr[Double] = js.undefined,
             tradeDateTime: js.UndefOr[js.Date] = js.undefined,
             commission: js.UndefOr[Double] = js.undefined,
             processedTime: js.UndefOr[js.Date] = js.undefined,
             cost: js.UndefOr[Double] = js.undefined,
             netValue: js.UndefOr[Double] = js.undefined): PositionData = {
      new PositionData(
        positionID = positionID ?? position.positionID,
        portfolioID = portfolioID ?? position.portfolioID,
        orderID = orderID ?? position.orderID,
        symbol = symbol ?? position.symbol,
        exchange = exchange ?? position.exchange,
        price = pricePaid ?? position.price,
        quantity = quantity ?? position.quantity,
        tradeDateTime = tradeDateTime ?? position.tradeDateTime,
        commission = commission ?? position.commission,
        processedTime = processedTime ?? position.processedTime,
        cost = cost ?? position.cost,
        netValue = netValue ?? position.netValue)
    }

  }

}