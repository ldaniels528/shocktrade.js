package com.shocktrade.javascript.models

import scala.scalajs.js

/**
  * Order Model
  * @author lawrence.daniels@gmail.com
  */
@js.native
trait Order extends js.Object {
  var _id: js.UndefOr[BSONObjectID]
  var symbol: String
  var accountType: String
  var orderType: String
  var priceType: String
  var price: Double
  var quantity: Double
  var commission: Double

}
