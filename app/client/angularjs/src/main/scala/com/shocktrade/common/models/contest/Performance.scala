package com.shocktrade.common.models.contest

import scala.scalajs.js

/**
  * Trading Performance Model
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
@js.native
class Performance(var _id: js.UndefOr[String] = js.undefined,
                  var symbol: js.UndefOr[String] = js.undefined,
                  var pricePaid: js.UndefOr[Double] = js.undefined,
                  var priceSold: js.UndefOr[Double] = js.undefined,
                  var quantity: js.UndefOr[Double] = js.undefined,
                  var commissions: js.UndefOr[Double] = js.undefined) extends PerformanceLike
