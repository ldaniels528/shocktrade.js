package com.shocktrade.models.quote

import java.util.Date

import com.shocktrade.util.BSONHelper._
import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json.{Reads, Writes, __}
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

  implicit val quoteSnapshotReads: Reads[QuoteSnapshot] = (
    (__ \ "symbol").read[String] and
      (__ \ "name").readNullable[String] and
      (__ \ "lastTrade").readNullable[Double] and
      (__ \ "tradeDateTime").readNullable[Date])(QuoteSnapshot.apply _)

  implicit val quoteSnapshotWrites: Writes[QuoteSnapshot] = (
    (__ \ "symbol").write[String] and
      (__ \ "name").writeNullable[String] and
      (__ \ "lastTrade").writeNullable[Double] and
      (__ \ "tradeDateTime").writeNullable[Date])(unlift(QuoteSnapshot.unapply))

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
