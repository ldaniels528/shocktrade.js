package com.shocktrade.models.quote

import play.api.libs.json.Json
import reactivemongo.bson.Macros

import scala.language.{implicitConversions, postfixOps}

/**
  * Represent a Market Quote
  * @author lawrence.daniels@gmail.com
  */
case class MarketQuote(symbol: String, name: Option[String], lastTrade: Option[Double], close: Option[Double])

/**
  * Market Quote Singleton
  * @author lawrence.daniels@gmail.com
  */
object MarketQuote {
  val Fields = Seq("name", "symbol", "lastTrade", "close")

  implicit val MarketQuoteFormat = Json.format[MarketQuote]

  implicit val MarketQuoteHandler = Macros.handler[MarketQuote]

}