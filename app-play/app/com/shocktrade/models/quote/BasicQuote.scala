package com.shocktrade.models.quote

import java.util.Date

import com.shocktrade.util.BSONHelper._
import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json.{Json, Reads, __}
import reactivemongo.bson.{BSONDocument, BSONDocumentReader, BSONDocumentWriter, _}

/**
 * Represents a Basic Quote
 */
case class BasicQuote(symbol: String,
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
                      low52Week: Option[Double] = None,
                      high52Week: Option[Double] = None,
                      volume: Option[Long] = None,
                      active: Option[Boolean] = None)

/**
 * Basic Quote Singleton
 */
object BasicQuote {
  val Fields = Seq("name", "symbol", "exchange", "open", "close", "lastTrade", "tradeDateTime",
    "high", "low", "high52Week", "low52Week", "spread", "changePct", "volume", "active")

  implicit val basicQuoteReads: Reads[BasicQuote] = {
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
      (__ \ "low52Week").readNullable[Double] and
      (__ \ "high52Week").readNullable[Double] and
      (__ \ "volume").readNullable[Long] and
      (__ \ "active").readNullable[Boolean])(BasicQuote.apply _)
  }

  implicit val basicQuoteWrites = Json.writes[BasicQuote]

  implicit object BasicQuoteReader extends BSONDocumentReader[BasicQuote] {
    override def read(doc: BSONDocument) = BasicQuote(
      doc.getAs[String]("symbol").get,
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
      doc.getAs[Double]("low52Week"),
      doc.getAs[Double]("high52Week"),
      doc.getAs[Long]("volume"),
      doc.getAs[Boolean]("active")
    )
  }

  implicit object BasicQuoteWriter extends BSONDocumentWriter[BasicQuote] {
    override def write(quote: BasicQuote) = BSONDocument(
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
      "low52Week" -> quote.low52Week,
      "high52Week" -> quote.high52Week,
      "volume" -> quote.volume,
      "active" -> quote.active
    )
  }

}