package com.shocktrade.models.quote

import play.api.libs.json._
import reactivemongo.bson.Macros

/**
  * Sector Quote
  * @param symbol    the given stock symbol/ticker
  * @param market    the market or exchange
  * @param sector    the sector for which the security is classified
  * @param industry  the industry for which the security is classified
  * @param lastTrade the last sale
  * @author lawrence.daniels@gmail.com
  */
case class SectorQuote(symbol: String,
                       market: Option[String],
                       sector: Option[String],
                       industry: Option[String],
                       subIndustry: Option[String],
                       lastTrade: Option[Double]) {

  def exchange: Option[String] = {
    market map (_.toUpperCase) map {
      case s if s.startsWith("NASD") || s.startsWith("NCM") || s.startsWith("NMS") => "NASDAQ"
      case s if s.startsWith("NYS") || s.startsWith("NYQ") => "NYSE"
      case s if s.startsWith("OTC") => "OTCBB"
      case s if s.startsWith("OTHER") => "OTCBB"
      case s if s == "PNK" => "OTCBB"
      case other => other
    }
  }

}

/**
  * Sector Quote Companion Object
  * @author lawrence.daniels@gmail.com
  */
object SectorQuote {
  val Fields = Seq("symbol", "exchange", "lastTrade", "industry", "sector", "subIndustry")

  implicit val SectorQuoteFormat = Json.format[SectorQuote]

  implicit val SectorQuoteHandler = Macros.handler[SectorQuote]

}