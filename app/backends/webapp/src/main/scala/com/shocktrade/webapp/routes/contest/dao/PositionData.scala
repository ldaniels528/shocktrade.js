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
                   val symbol: js.UndefOr[String],
                   val exchange: js.UndefOr[String],
                   val quantity: js.UndefOr[Double],
                   val businessName: js.UndefOr[String] = js.undefined,
                   val processedTime: js.UndefOr[js.Date] = js.undefined) extends PositionLike

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
             businessName: js.UndefOr[String],
             symbol: js.UndefOr[String] = js.undefined,
             exchange: js.UndefOr[String] = js.undefined,
             quantity: js.UndefOr[Double] = js.undefined,
             processedTime: js.UndefOr[js.Date] = js.undefined): PositionData = {
      new PositionData(
        positionID = positionID ?? position.positionID,
        portfolioID = portfolioID ?? position.portfolioID,
        businessName = businessName ?? position.businessName,
        symbol = symbol ?? position.symbol,
        exchange = exchange ?? position.exchange,
        quantity = quantity ?? position.quantity,
        processedTime = processedTime ?? position.processedTime)
    }

    def portfolioID_! : String = position.portfolioID.getOrElse(throw js.JavaScriptException("portfolioID missing in position"))

  }

}