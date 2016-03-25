package com.shocktrade.models.quote

import java.util.Date

import play.api.libs.json.Json
import reactivemongo.bson._

/**
  * Represents a Research Quote
  * @author lawrence.daniels@gmail.com
  */
case class ResearchQuote(symbol: String,
                         name: Option[String] = None,
                         exchange: Option[String] = None,
                         lastTrade: Option[Double] = None,
                         tradeDateTime: Option[Date] = None,
                         changePct: Option[Double] = None,
                         prevClose: Option[Double] = None,
                         open: Option[Double] = None,
                         close: Option[Double] = None,
                         low: Option[Double] = None,
                         high: Option[Double] = None,
                         spread: Option[Double] = None,
                         volume: Option[Long] = None)

/**
  * Research Quote Singleton
  * @author lawrence.daniels@gmail.com
  */
object ResearchQuote {
  val Fields = Seq("name", "symbol", "exchange", "open", "close", "lastTrade",
    "tradeDateTime", "high", "low", "spread", "changePct", "volume")

  implicit val ResearchQuoteFormat = Json.format[ResearchQuote]

  implicit val ResearchQuoteHandler = Macros.handler[ResearchQuote]

}
