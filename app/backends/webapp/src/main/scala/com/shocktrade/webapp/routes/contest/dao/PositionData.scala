package com.shocktrade.webapp.routes.contest.dao

import java.util.UUID

import com.shocktrade.common.models.contest.PositionLike
import io.scalajs.util.JsUnderOrHelper._

import scala.scalajs.js

/**
 * Position Data
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
class PositionData(var positionID: js.UndefOr[String] = UUID.randomUUID().toString,
                   var portfolioID: js.UndefOr[String],
                   var symbol: js.UndefOr[String],
                   var exchange: js.UndefOr[String],
                   var price: js.UndefOr[Double],
                   var quantity: js.UndefOr[Double],
                   var commission: js.UndefOr[Double],
                   var processedTime: js.UndefOr[js.Date],
                   var netValue: js.UndefOr[Double]) extends PositionLike

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
             symbol: js.UndefOr[String] = js.undefined,
             exchange: js.UndefOr[String] = js.undefined,
             pricePaid: js.UndefOr[Double] = js.undefined,
             quantity: js.UndefOr[Double] = js.undefined,
             commission: js.UndefOr[Double] = js.undefined,
             processedTime: js.UndefOr[js.Date] = js.undefined,
             netValue: js.UndefOr[Double] = js.undefined): PositionData = {
      new PositionData(
        positionID = positionID ?? position.positionID,
        portfolioID = portfolioID ?? position.portfolioID,
        symbol = symbol ?? position.symbol,
        exchange = exchange ?? position.exchange,
        price = pricePaid ?? position.price,
        quantity = quantity ?? position.quantity,
        commission = commission ?? position.commission,
        processedTime = processedTime ?? position.processedTime,
        netValue = netValue ?? position.netValue)
    }

  }

}