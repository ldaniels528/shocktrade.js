package com.shocktrade.models.quote

import java.util.Date

import com.shocktrade.util.BSONHelper._
import play.api.libs.json.Json
import reactivemongo.bson.{BSONDocument, BSONDocumentReader, BSONDocumentWriter}

import scala.language.{implicitConversions, postfixOps}

/**
 * Quote Snapshot
 * @param name the company name
 * @param symbol the stock symbol/ticker
 * @param lastTrade the last sale amount
 * @param tradeDate the trade date
 */
case class QuoteSnapshot(symbol: String, name: Option[String], lastTrade: Option[Double], tradeDate: Option[Date])

/**
 * Quote Snapshot Singleton
 */
object QuoteSnapshot {
  val Fields = Seq("symbol", "name", "lastTrade", "tradeDateTime")

  implicit val quoteSnapshotReads = Json.reads[QuoteSnapshot]
  implicit val quoteSnapshotWrites = Json.writes[QuoteSnapshot]

  implicit object QuoteSnapshotReader extends BSONDocumentReader[QuoteSnapshot] {
    def read(doc: BSONDocument) = QuoteSnapshot(
      doc.getAs[String]("symbol").get,
      doc.getAs[String]("name"),
      doc.getAs[Double]("lastTrade"),
      doc.getAs[Date]("tradeDateTime")
    )
  }

  implicit object QuoteSnapshotWriter extends BSONDocumentWriter[QuoteSnapshot] {
    def write(quote: QuoteSnapshot) = BSONDocument(
      "symbol" -> quote.symbol,
      "name" -> quote.name,
      "lastTrade" -> quote.lastTrade,
      "tradeDateTime" -> quote.tradeDate
    )
  }

}
