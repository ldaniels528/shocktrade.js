package com.shocktrade.javascript.models

import scala.scalajs.js

/**
  * Performance Model
  * @author lawrence.daniels@gmail.com
  */
@js.native
trait Performance extends js.Object {
  var _id: js.UndefOr[BSONObjectID]
  var symbol: String
  var pricePaid: Double
  var priceSold: Double
  var quantity: Double
  var commissions: Double

}
