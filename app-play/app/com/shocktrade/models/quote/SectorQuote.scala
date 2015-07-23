package com.shocktrade.models.quote

import play.api.libs.json._
import reactivemongo.bson.Macros

/**
 * Sector Quote
 * @param symbol the given stock symbol/ticker
 * @param market the market or exchange
 * @param sector the sector for which the security is classified
 * @param industry the industry for which the security is classified
 * @param lastTrade the last sale
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
 * Sector Quote
 */
object SectorQuote {
  val Fields = Seq("symbol", "exchange", "lastTrade", "industry", "sector", "subIndustry")

  implicit val sectorQuoteReads = Json.reads[SectorQuote]
  implicit val sectorQuoteWrites = Json.writes[SectorQuote]
  implicit val sectorQuoteReader = Macros.handler[SectorQuote]

}