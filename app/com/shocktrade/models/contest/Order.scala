package com.shocktrade.models.contest

import java.util.Date

import com.shocktrade.util.BSONHelper
import BSONHelper._
import play.api.libs.json.Json.{obj => JS}
import play.modules.reactivemongo.json.BSONFormats._
import reactivemongo.bson.BSONObjectID

/**
 * Represents a stock order
 * @author lawrence.daniels@gmail.com
 */
case class Order(symbol: String,
                 exchange: String,
                 creationTime: Date,
                 expirationTime: Option[Date] = None,
                 orderType: OrderType,
                 price: BigDecimal,
                 priceType: PriceType,
                 processedTime: Option[Date] = None,
                 quantity: Int,
                 commission: BigDecimal,
                 id: Option[BSONObjectID] = None) {

  def toJson = JS(
    "_id" -> id.toBSID,
    "symbol" -> symbol,
    "exchange" -> exchange,
    "expirationTime" -> expirationTime,
    "orderType" -> orderType.name,
    "price" -> price,
    "priceType" -> priceType.name,
    "processedTime" -> processedTime,
    "quantity" -> quantity,
    "commission" -> commission
  )

}
