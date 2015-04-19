package com.shocktrade.actors.quote

import play.api.libs.json.JsObject

/**
 * Represents the collection of messages shared by Stock Quote actors
 * @author lawrence.daniels@gmail.com
 */
object QuoteMessages {

  case class GetQuote(symbol: String)

  case class GetFullQuote(symbol: String)

  case class GetQuotes(symbols: Seq[String])

  case class SaveQuote(symbol: String, quote: JsObject)

}
