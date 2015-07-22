package com.shocktrade.models.quote

import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json._
import reactivemongo.bson.{BSONDocumentWriter, BSONDocument, BSONDocumentReader}

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

  implicit val sectorQuoteReads: Reads[SectorQuote] = (
    (__ \ "symbol").read[String] and
      (__ \ "exchange").readNullable[String] and
      (__ \ "sector").readNullable[String] and
      (__ \ "industry").readNullable[String] and
      (__ \ "subIndustry").readNullable[String] and
      (__ \ "lastTrade").readNullable[Double])(SectorQuote.apply _)

  implicit val sectorQuoteWrites: Writes[SectorQuote] = (
    (__ \ "symbol").write[String] and
      (__ \ "exchange").writeNullable[String] and
      (__ \ "sector").writeNullable[String] and
      (__ \ "industry").writeNullable[String] and
      (__ \ "subIndustry").writeNullable[String] and
      (__ \ "lastTrade").writeNullable[Double])(unlift(SectorQuote.unapply))

  implicit object SectorQuoteReader extends BSONDocumentReader[SectorQuote] {
    override def read(doc: BSONDocument) = SectorQuote(
      doc.getAs[String]("symbol").get,
      doc.getAs[String]("exchange"),
      doc.getAs[String]("sector"),
      doc.getAs[String]("industry"),
      doc.getAs[String]("subIndustry"),
      doc.getAs[Double]("lastTrade")
    )
  }

  implicit object SectorQuoteWriter extends BSONDocumentWriter[MarketQuote] {
    override def write(quote: MarketQuote) = BSONDocument(
      "symbol" -> quote.symbol,
      "exchange" -> quote.name,
      "sector" -> quote.name,
      "industry" -> quote.name,
      "subIndustry" -> quote.name,
      "lastTrade" -> quote.lastTrade
    )
  }

}