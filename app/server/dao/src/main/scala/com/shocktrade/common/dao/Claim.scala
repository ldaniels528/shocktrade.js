package com.shocktrade.common.dao

import scala.scalajs.js
import scala.scalajs.js.annotation.ScalaJSDefined

@ScalaJSDefined
class Claim(val symbol: String,
            val exchange: String,
            val price: Double,
            val quantity: Double,
            val asOfTime: js.Date) extends js.Object
