package com.shocktrade.common.models.contest

import scala.scalajs.js

/**
 * Represents a Position model
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
class Position(var positionID: js.UndefOr[String],
               var portfolioID: js.UndefOr[String],
               var symbol: js.UndefOr[String],
               var exchange: js.UndefOr[String],
               var quantity: js.UndefOr[Double],
               var processedTime: js.UndefOr[js.Date]) extends PositionLike {

  // UI-specific fields
  var lastTrade: js.UndefOr[Double] = js.undefined
  var gainLossPct: js.UndefOr[Double] = js.undefined

}
