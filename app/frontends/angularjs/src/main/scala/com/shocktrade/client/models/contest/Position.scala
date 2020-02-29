package com.shocktrade.client.models.contest

import com.shocktrade.common.models.contest.PositionLike

import scala.scalajs.js

/**
  * Represents a Position model
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
class Position(var _id: js.UndefOr[String],
               var symbol: js.UndefOr[String],
               var exchange: js.UndefOr[String],
               var pricePaid: js.UndefOr[Double],
               var quantity: js.UndefOr[Double],
               var commission: js.UndefOr[Double],
               var processedTime: js.UndefOr[js.Date],
               var accountType: js.UndefOr[String],
               var netValue: js.UndefOr[Double]) extends PositionLike {

  // UI-specific fields
  var lastTrade: js.UndefOr[Double] = js.undefined
  var gainLossPct: js.UndefOr[Double] = js.undefined

}
