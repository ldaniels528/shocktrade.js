package com.shocktrade.server.dao.contest

import com.shocktrade.common.models.contest.PerformanceLike

import scala.scalajs.js
import scala.scalajs.js.annotation.ScalaJSDefined

/**
  * Trading Performance Model
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
@ScalaJSDefined
class PerformanceData(var _id: js.UndefOr[String] = js.undefined,
                      var symbol: js.UndefOr[String] = js.undefined,
                      var pricePaid: js.UndefOr[Double] = js.undefined,
                      var priceSold: js.UndefOr[Double] = js.undefined,
                      var quantity: js.UndefOr[Double] = js.undefined,
                      var commissions: js.UndefOr[Double] = js.undefined) extends PerformanceLike