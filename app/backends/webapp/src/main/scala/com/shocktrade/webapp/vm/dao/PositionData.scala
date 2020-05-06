package com.shocktrade.webapp.vm.dao

import com.shocktrade.common.models.contest.PositionLike
import io.scalajs.util.JsUnderOrHelper._

import scala.scalajs.js

/**
 * Position Data
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
class PositionData(val positionID: js.UndefOr[String],
                   val portfolioID: js.UndefOr[String],
                   val symbol: js.UndefOr[String],
                   val marketValue: js.UndefOr[Double],
                   val exchange: js.UndefOr[String],
                   val quantity: js.UndefOr[Double],
                   val processedTime: js.UndefOr[js.Date]) extends PositionLike

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
             marketValue: js.UndefOr[Double] = js.undefined,
             exchange: js.UndefOr[String] = js.undefined,
             quantity: js.UndefOr[Double] = js.undefined,
             processedTime: js.UndefOr[js.Date] = js.undefined): PositionData = {
      new PositionData(
        positionID = positionID ?? position.positionID,
        portfolioID = portfolioID ?? position.portfolioID,
        symbol = symbol ?? position.symbol,
        marketValue = marketValue ?? position.marketValue,
        exchange = exchange ?? position.exchange,
        quantity = quantity ?? position.quantity,
        processedTime = processedTime ?? position.processedTime)
    }

    def portfolioID_! : String = position.portfolioID.getOrElse(throw js.JavaScriptException("portfolioID missing in position"))

  }

}