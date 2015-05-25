package com.shocktrade.actors

import com.shocktrade.models.quote.QuoteFilter
import play.api.libs.json.JsObject

/**
 * Represents the collection of messages shared by Stock Quote actors
 * @author lawrence.daniels@gmail.com
 */
object QuoteMessages {

  case class FindQuotes(filter: QuoteFilter)

  case class GetQuote(symbol: String)

  case class GetFullQuote(symbol: String)

  case class GetQuotes(symbols: Seq[String])

  case class SaveQuote(symbol: String, quote: JsObject)

}
