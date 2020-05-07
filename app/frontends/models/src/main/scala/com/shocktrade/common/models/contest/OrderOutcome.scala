package com.shocktrade.common.models.contest

import scala.scalajs.js

class OrderOutcome(val fulfilled: Boolean,
                   val w: Int,
                   val negotiatedPrice: js.UndefOr[Double] = js.undefined,
                   val xp: js.UndefOr[Double] = js.undefined) extends js.Object