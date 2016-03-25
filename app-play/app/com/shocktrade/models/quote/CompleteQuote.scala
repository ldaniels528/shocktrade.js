package com.shocktrade.models.quote

import java.util.Date

import play.api.libs.json.Json
import reactivemongo.bson._

/**
  * Represents a real-time quote
  * @author lawrence.daniels@gmail.com
  */
case class CompleteQuote(symbol: String,
                         name: Option[String] = None,
                         exchange: Option[String] = None,
                         lastTrade: Option[Double] = None,
                         tradeDateTime: Option[Date] = None,
                         change: Option[Double] = None,
                         changePct: Option[Double] = None,
                         prevClose: Option[Double] = None,
                         open: Option[Double] = None,
                         close: Option[Double] = None,
                         low: Option[Double] = None,
                         high: Option[Double] = None,
                         //low52Week: Option[Double] = None,
                         //high52Week: Option[Double] = None,
                         volume: Option[Long] = None,
                         avgVol3m: Option[Long] = None,
                         marketCap: Option[Double] = None,
                         active: Option[Boolean] = None,
                         products: List[ETFProduct] = Nil,
                         askBid: AskBid = AskBid(),
                         industryCodes: IndustryCodes = IndustryCodes(),
                         valuation: Valuation = Valuation()) {

  def spreadPct: Option[Double] = for {
    l <- low if l != 0.0d
    h <- high
  } yield (h - l) / l

}

/**
  * Complete Quote Singleton
  * @author lawrence.daniels@gmail.com
  */
object CompleteQuote {

  implicit val CompleteQuoteFormat = Json.format[CompleteQuote]

  implicit val CompleteQuoteHandler = Macros.handler[CompleteQuote]

}

