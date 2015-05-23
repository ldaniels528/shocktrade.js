package com.shocktrade.models.contest

import play.api.libs.json.{Format, JsString, JsSuccess, JsValue}
import reactivemongo.bson.{BSONHandler, BSONString}

/**
 * Represents an enumeration of Stock Order Price Types
 * @author lawrence.daniels@gmail.com
 */
object PriceTypes extends Enumeration {
  type PriceType = Value

  val LIMIT = Value("LIMIT")
  val MARKET = Value("MARKET")
  val MARKET_ON_CLOSE = Value("MARKET_ON_CLOSE")
  val STOP_LIMIT = Value("STOP_LIMIT")

  /**
   * Price Type Format
   * @author lawrence.daniels@gmail.com
   */
  implicit object PriceTypeFormat extends Format[PriceType] {

    def reads(json: JsValue) = JsSuccess(PriceTypes.withName(json.as[String]))

    def writes(priceType: PriceType) = JsString(priceType.toString)
  }

  /**
   * Price Type Handler
   * @author lawrence.daniels@gmail.com
   */
  implicit object PriceTypeHandler extends BSONHandler[BSONString, PriceType] {

    def read(string: BSONString) = PriceTypes.withName(string.value)

    def write(priceType: PriceType) = BSONString(priceType.toString)
  }

}
