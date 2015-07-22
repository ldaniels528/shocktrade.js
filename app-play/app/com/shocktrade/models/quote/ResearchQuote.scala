package com.shocktrade.models.quote

import java.util.Date

import com.shocktrade.util.BSONHelper._
import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json.{Reads, Writes, __}
import reactivemongo.bson.{BSONDocument, BSONDocumentReader, BSONDocumentWriter, _}

/**
 * Represents a Research Quote
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
 */
object ResearchQuote {
  val Fields = Seq("name", "symbol", "exchange", "open", "close", "lastTrade",
    "tradeDateTime", "high", "low", "spread", "changePct", "volume")

  implicit val researchQuoteReads: Reads[ResearchQuote] = {
    ((__ \ "symbol").read[String] and
      (__ \ "name").readNullable[String] and
      (__ \ "exchange").readNullable[String] and
      (__ \ "lastTrade").readNullable[Double] and
      (__ \ "tradeDateTime" \ "$date").readNullable[Long].map(_.map(t => new Date(t))) and
      (__ \ "changePct").readNullable[Double] and
      (__ \ "prevClose").readNullable[Double] and
      (__ \ "open").readNullable[Double] and
      (__ \ "close").readNullable[Double] and
      (__ \ "low").readNullable[Double] and
      (__ \ "high").readNullable[Double] and
      (__ \ "spread").readNullable[Double] and
      (__ \ "volume").readNullable[Long])(ResearchQuote.apply _)
  }

  implicit val researchQuoteWrites: Writes[ResearchQuote] = (
    (__ \ "symbol").write[String] and
      (__ \ "name").writeNullable[String] and
      (__ \ "exchange").writeNullable[String] and
      (__ \ "lastTrade").writeNullable[Double] and
      (__ \ "tradeDateTime").writeNullable[Date] and
      (__ \ "changePct").writeNullable[Double] and
      (__ \ "prevClose").writeNullable[Double] and
      (__ \ "open").writeNullable[Double] and
      (__ \ "close").writeNullable[Double] and
      (__ \ "low").writeNullable[Double] and
      (__ \ "high").writeNullable[Double] and
      (__ \ "spread").writeNullable[Double] and
      (__ \ "volume").writeNullable[Long])(unlift(ResearchQuote.unapply))

  implicit object ResearchQuoteReader extends BSONDocumentReader[ResearchQuote] {
    def read(doc: BSONDocument) = ResearchQuote(
      doc.getAs[String]("symbol").getOrElse("N/A"),
      doc.getAs[String]("name"),
      doc.getAs[String]("exchange"),
      doc.getAs[Double]("lastTrade"),
      doc.getAs[Date]("tradeDateTime"),
      doc.getAs[Double]("changePct"),
      doc.getAs[Double]("prevClose"),
      doc.getAs[Double]("open"),
      doc.getAs[Double]("close"),
      doc.getAs[Double]("low"),
      doc.getAs[Double]("high"),
      doc.getAs[Double]("spread"),
      doc.getAs[Long]("volume")
    )
  }

  implicit object ResearchQuoteWriter extends BSONDocumentWriter[ResearchQuote] {
    def write(quote: ResearchQuote) = BSONDocument(
      "symbol" -> quote.symbol,
      "name" -> quote.name,
      "exchange" -> quote.exchange,
      "lastTrade" -> quote.lastTrade,
      "tradeDateTime" -> quote.tradeDateTime,
      "changePct" -> quote.changePct,
      "prevClose" -> quote.prevClose,
      "open" -> quote.open,
      "close" -> quote.close,
      "low" -> quote.low,
      "high" -> quote.high,
      "spread" -> quote.spread,
      "volume" -> quote.volume
    )
  }

}
