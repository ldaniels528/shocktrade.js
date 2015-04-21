package com.shocktrade.models.contest

import java.util.Date

import com.shocktrade.util.BSONHelper
import BSONHelper._
import play.api.libs.json.Json.{obj => JS}
import play.modules.reactivemongo.json.BSONFormats._
import reactivemongo.bson.BSONObjectID

/**
 * Represents a financial position
 * @author lawrence.daniels@gmail.com
 */
case class Position(symbol: String,
                    exchange: String,
                    pricePaid: BigDecimal,
                    quantity: Int,
                    commission: Commission,
                    purchasedDate: Date,
                    id: Option[BSONObjectID] = None) {

  def toJson = JS(
    "_id" -> id.toBSID,
    "symbol" -> symbol,
    "exchange" -> exchange,
    "pricePaid" -> pricePaid,
    "quantity" -> quantity,
    "commission" -> commission.toJson,
    "purchasedDate" -> purchasedDate
  )

}
