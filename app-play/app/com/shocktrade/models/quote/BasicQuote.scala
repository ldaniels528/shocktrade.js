package com.shocktrade.models.quote

import java.util.Date

import play.api.libs.json.Json
import reactivemongo.bson._

/**
  * Represents a Basic Quote
  * @author lawrence.daniels@gmail.com
  */
case class BasicQuote(symbol: String,
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
                      low52Week: Option[Double] = None,
                      high52Week: Option[Double] = None,
                      volume: Option[Long] = None,
                      name: Option[String] = None,
                      active: Option[Boolean] = None)

/**
  * Basic Quote Singleton
  * @author lawrence.daniels@gmail.com
  */
object BasicQuote {
  val Fields = Seq("symbol", "exchange", "lastTrade", "industry", "sector", "subIndustry")

  implicit val BasicQuoteFormat = Json.format[BasicQuote]

  implicit val BasicQuoteHandler = Macros.handler[BasicQuote]

}