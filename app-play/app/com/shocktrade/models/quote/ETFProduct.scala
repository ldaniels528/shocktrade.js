package com.shocktrade.models.quote

import play.api.libs.json.Json
import reactivemongo.bson._

/**
  * Represents an ETF Product
  * @author lawrence.daniels@gmail.com
  */
case class ETFProduct(name: String, symbol: String, netAssetsPct: Double)

/**
  * ETF Product Singleton
  * @author lawrence.daniels@gmail.com
  */
object ETFProduct {

  implicit val ETFProductFormat = Json.format[ETFProduct]

  implicit val ETFProductHandler = Macros.handler[ETFProduct]

}