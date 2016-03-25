package com.shocktrade.models.quote

import play.api.libs.json._
import reactivemongo.bson.Macros

/**
  * Product Quote
  * @author lawrence.daniels@gmail.com
  */
case class ProductQuote(symbol: String,
                        exchange: Option[String] = None,
                        lastTrade: Option[Double] = None,
                        open: Option[Double] = None,
                        close: Option[Double] = None,
                        change: Option[Double] = None,
                        changePct: Option[Double] = None,
                        spread: Option[Double] = None,
                        volume: Option[Long] = None,
                        name: Option[String] = None,
                        sector: Option[String],
                        industry: Option[String],
                        active: Option[Boolean] = None)

/**
  * Product Quote Singleton
  * @author lawrence.daniels@gmail.com
  */
object ProductQuote {
  val Fields = Seq("symbol", "exchange", "lastTrade", "open", "close", "change", "changePct", "spread", "volume", "name", "sector", "industry", "active")

  implicit val ProductQuoteFormat = Json.format[ProductQuote]

  implicit val ProductQuoteHandler = Macros.handler[ProductQuote]

}
