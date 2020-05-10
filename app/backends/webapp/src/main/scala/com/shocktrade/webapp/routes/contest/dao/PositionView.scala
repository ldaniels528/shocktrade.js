package com.shocktrade.webapp.routes.contest.dao

import com.shocktrade.common.models.contest.PositionLike
import io.scalajs.util.JsUnderOrHelper._

import scala.scalajs.js

/**
 * Position View
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
class PositionView(val positionID: js.UndefOr[String],
                   val portfolioID: js.UndefOr[String],
                   val businessName: js.UndefOr[String],
                   val symbol: js.UndefOr[String],
                   val exchange: js.UndefOr[String],
                   val lastTrade: js.UndefOr[Double],
                   val high: js.UndefOr[Double],
                   val low: js.UndefOr[Double],
                   val marketValue: js.UndefOr[Double],
                   val quantity: js.UndefOr[Double],
                   val processedTime: js.UndefOr[js.Date]) extends PositionLike

/**
 * Position View Companion
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
object PositionView {

  /**
   * Position View Enrichment
   * @param position the given [[PositionView position]]
   */
  final implicit class PositionViewEnrichment(val position: PositionView) extends AnyVal {

    @inline
    def copy(positionID: js.UndefOr[String] = js.undefined,
             portfolioID: js.UndefOr[String] = js.undefined,
             businessName: js.UndefOr[String] = js.undefined,
             symbol: js.UndefOr[String] = js.undefined,
             exchange: js.UndefOr[String] = js.undefined,
             lastTrade: js.UndefOr[Double] = js.undefined,
             high: js.UndefOr[Double] = js.undefined,
             low: js.UndefOr[Double] = js.undefined,
             marketValue: js.UndefOr[Double] = js.undefined,
             quantity: js.UndefOr[Double] = js.undefined,
             processedTime: js.UndefOr[js.Date] = js.undefined): PositionView = {
      new PositionView(
        positionID = positionID ?? position.positionID,
        portfolioID = portfolioID ?? position.portfolioID,
        businessName = businessName ?? position.businessName,
        symbol = symbol ?? position.symbol,
        exchange = exchange ?? position.exchange,
        lastTrade = lastTrade ?? position.lastTrade,
        high = high ?? position.high,
        low = low ?? position.low,
        marketValue = marketValue ?? position.marketValue,
        quantity = quantity ?? position.quantity,
        processedTime = processedTime ?? position.processedTime)
    }

  }

}