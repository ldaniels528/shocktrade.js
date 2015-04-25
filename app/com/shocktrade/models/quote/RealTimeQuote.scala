package com.shocktrade.models.quote

import java.util.Date

import com.shocktrade.util.BSONHelper._
import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json.{Reads, Writes, __}
import reactivemongo.bson.{BSONDocument, BSONDocumentReader, BSONDocumentWriter}

/**
 * Represents a real-time quote
 * @author lawrence.daniels@gmail.com
 */
case class RealTimeQuote(symbol: String,
                         name: Option[String],
                         exchange: Option[String],
                         lastTrade: Option[Double],
                         tradeDateTime: Option[Date],
                         change: Option[Double],
                         changePct: Option[Double],
                         prevClose: Option[Double],
                         open: Option[Double],
                         close: Option[Double],
                         ask: Option[Double],
                         bid: Option[Double],
                         target1Yr: Option[Double],
                         beta: Option[Double],
                         low: Option[Double],
                         high: Option[Double],
                         low52Week: Option[Double],
                         high52Week: Option[Double],
                         volume: Option[Long],
                         avgVol3m: Option[Long],
                         marketCap: Option[Double])

/**
 * Real-time Quote
 * @author lawrence.daniels@gmail.com
 */
object RealTimeQuote {

  implicit val realTimeQuoteReads: Reads[RealTimeQuote] = (
    (__ \ "symbol").read[String] and
      (__ \ "name").readNullable[String] and
      (__ \ "exchange").readNullable[String] and
      (__ \ "lastTrade").readNullable[Double] and
      (__ \ "tradeDateTime").readNullable[Date] and
      (__ \ "change").readNullable[Double] and
      (__ \ "changePct").readNullable[Double] and
      (__ \ "prevClose").readNullable[Double] and
      (__ \ "open").readNullable[Double] and
      (__ \ "close").readNullable[Double] and
      (__ \ "ask").readNullable[Double] and
      (__ \ "bid").readNullable[Double] and
      (__ \ "target1Yr").readNullable[Double] and
      (__ \ "beta").readNullable[Double] and
      (__ \ "low").readNullable[Double] and
      (__ \ "high").readNullable[Double] and
      (__ \ "low52Week").readNullable[Double] and
      (__ \ "high52Week").readNullable[Double] and
      (__ \ "volume").readNullable[Long] and
      (__ \ "avgVol3m").readNullable[Long] and
      (__ \ "marketCap").readNullable[Double])(RealTimeQuote.apply _)

  implicit val realTimeQuoteWrites: Writes[RealTimeQuote] = (
    (__ \ "symbol").write[String] and
      (__ \ "name").writeNullable[String] and
      (__ \ "exchange").writeNullable[String] and
      (__ \ "lastTrade").writeNullable[Double] and
      (__ \ "tradeDateTime").writeNullable[Date] and
      (__ \ "change").writeNullable[Double] and
      (__ \ "changePct").writeNullable[Double] and
      (__ \ "prevClose").writeNullable[Double] and
      (__ \ "open").writeNullable[Double] and
      (__ \ "close").writeNullable[Double] and
      (__ \ "ask").writeNullable[Double] and
      (__ \ "bid").writeNullable[Double] and
      (__ \ "target1Yr").writeNullable[Double] and
      (__ \ "beta").writeNullable[Double] and
      (__ \ "low").writeNullable[Double] and
      (__ \ "high").writeNullable[Double] and
      (__ \ "low52Week").writeNullable[Double] and
      (__ \ "high52Week").writeNullable[Double] and
      (__ \ "volume").writeNullable[Long] and
      (__ \ "avgVol3m").writeNullable[Long] and
      (__ \ "marketCap").writeNullable[Double])(unlift(RealTimeQuote.unapply))

  implicit object RealTimeQuoteReader extends BSONDocumentReader[RealTimeQuote] {
    def read(doc: BSONDocument) = RealTimeQuote(
      doc.getAs[String]("symbol").get,
      doc.getAs[String]("name"),
      doc.getAs[String]("exchange"),
      doc.getAs[Double]("lastTrade"),
      doc.getAs[Date]("tradeDateTime"),
      doc.getAs[Double]("change"),
      doc.getAs[Double]("changePct"),
      doc.getAs[Double]("prevClose"),
      doc.getAs[Double]("open"),
      doc.getAs[Double]("close"),
      doc.getAs[Double]("ask"),
      doc.getAs[Double]("bid"),
      doc.getAs[Double]("target1Yr"),
      doc.getAs[Double]("beta"),
      doc.getAs[Double]("low"),
      doc.getAs[Double]("high"),
      doc.getAs[Double]("low52Week"),
      doc.getAs[Double]("high52Week"),
      doc.getAs[Long]("volume"),
      doc.getAs[Long]("avgVol3m"),
      doc.getAs[Double]("marketCap")
    )
  }

  implicit object RealTimeQuoteWriter extends BSONDocumentWriter[RealTimeQuote] {
    def write(quote: RealTimeQuote) = BSONDocument(
      "symbol" -> quote.symbol,
      "name" -> quote.name,
      "exchange" -> quote.exchange,
      "lastTrade" -> quote.lastTrade,
      "tradeDateTime" -> quote.tradeDateTime,
      "change" -> quote.change,
      "changePct" -> quote.changePct,
      "prevClose" -> quote.prevClose,
      "open" -> quote.open,
      "close" -> quote.close,
      "ask" -> quote.ask,
      "bid" -> quote.bid,
      "target1Yr" -> quote.target1Yr,
      "beta" -> quote.beta,
      "low" -> quote.low,
      "high" -> quote.high,
      "low52Week" -> quote.low52Week,
      "high52Week" -> quote.high52Week,
      "volume" -> quote.volume,
      "avgVol3m" -> quote.avgVol3m,
      "marketCap" -> quote.marketCap
    )
  }

}

