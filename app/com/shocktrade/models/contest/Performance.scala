package com.shocktrade.models.contest

import java.util.Date

import com.shocktrade.util.BSONHelper
import BSONHelper._
import play.api.libs.json.JsArray
import play.api.libs.json.Json.{obj => JS}
import play.modules.reactivemongo.json.BSONFormats._
import reactivemongo.bson.BSONObjectID

/**
 * Represents the performance associated with the sell of a position
 * @author lawrence.daniels@gmail.com
 */
case class Performance(symbol: String,
                       exchange: String,
                       pricePaid: BigDecimal,
                       priceSold: BigDecimal,
                       quantity: Int,
                       commissions: List[Commission] = Nil,
                       purchasedDate: Date,
                       soldDate: Date,
                       id: Option[BSONObjectID] = None) {


  def toJson = JS(
    "_id" -> id.toBSID,
    "symbol" -> symbol,
    "exchange" -> exchange,
    "pricePaid" -> pricePaid,
    "priceSold" -> priceSold,
    "quantity" -> quantity,
    "commissions" -> JsArray(commissions.map(_.toJson))
  )

}

