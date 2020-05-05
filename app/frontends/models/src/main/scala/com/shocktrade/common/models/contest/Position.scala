package com.shocktrade.common.models.contest

import scala.scalajs.js

/**
 * Represents a Position model
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
class Position(val positionID: js.UndefOr[String],
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