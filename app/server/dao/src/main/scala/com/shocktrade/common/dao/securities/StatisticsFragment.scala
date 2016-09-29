package com.shocktrade.common.dao.securities

import scala.scalajs.js
import scala.scalajs.js.annotation.ScalaJSDefined

/**
  * Statistics Quote
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
@ScalaJSDefined
class StatisticsFragment(val symbol: String,
                         val avgVolume10Day: js.UndefOr[Double],
                         val beta: js.UndefOr[Double]) extends js.Object