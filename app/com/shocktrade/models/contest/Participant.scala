package com.shocktrade.models.contest

import java.util.Date

import com.shocktrade.util.BSONHelper
import BSONHelper._
import play.api.libs.json.JsArray
import play.api.libs.json.Json.{obj => JS}
import play.modules.reactivemongo.json.BSONFormats._
import reactivemongo.bson.BSONObjectID

/**
 * Represents a contest participant
 * @author lawrence.daniels@gmail.com
 */
case class Participant(name: String,
                       facebookId: String,
                       fundsAvailable: BigDecimal,
                       score: Int = 0,
                       lastTradeTime: Option[Date] = None,
                       orders: List[Order] = Nil,
                       orderHistory: List[Order] = Nil,
                       positions: List[Position] = Nil,
                       performance: List[Performance] = Nil,
                       id: Option[BSONObjectID] = None) {

  def toJson = JS(
    "_id" -> id.toBSID,
    "name" -> name,
    "facebookID" -> facebookId,
    "fundsAvailable" -> fundsAvailable,
    "score" -> score,
    "lastTradeTime" -> lastTradeTime,
    "orders" -> JsArray(orders map (_.toJson)),
    "orderHistory" -> JsArray(orderHistory map (_.toJson)),
    "positions" -> JsArray(positions map (_.toJson)),
    "performance" -> JsArray(performance map (_.toJson))
  )

}
