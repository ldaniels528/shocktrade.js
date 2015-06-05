package com.shocktrade.models.contest

import play.api.libs.json.{Format, JsString, JsSuccess, JsValue}
import reactivemongo.bson._

/**
 * Represents an enumeration of Stock Order Types
 * @author lawrence.daniels@gmail.com
 */
object OrderTypes extends Enumeration {
  type OrderType = Value

  val BUY = Value("BUY")
  val SELL = Value("SELL")

  /**
   * Order Type Format
   * @author lawrence.daniels@gmail.com
   */
  implicit object OrderTypeFormat extends Format[OrderType] {

    def reads(json: JsValue) = JsSuccess(OrderTypes.withName(json.as[String]))

    def writes(orderType: OrderType) = JsString(orderType.toString)
  }

  /**
   * Order Type Handler
   * @author lawrence.daniels@gmail.com
   */
  implicit object OrderTypeHandler extends BSONHandler[BSONString, OrderType] {

    def read(string: BSONString) = OrderTypes.withName(string.value)

    def write(orderType: OrderType) = BSONString(orderType.toString)
  }

}
