package com.shocktrade.models.quote

import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json.{Reads, Writes, __}
import reactivemongo.bson.{BSONDocument, BSONDocumentReader, BSONDocumentWriter}

import scala.language.{implicitConversions, postfixOps}

/**
 * Represent a Market Quote
 */
case class MarketQuote(symbol: String, name: Option[String], lastTrade: Option[Double], close: Option[Double])

/**
 * Market Quote Singleton
 */
object MarketQuote {
  val Fields = Seq("name", "symbol", "lastTrade", "close")

  implicit val marketQuoteReads: Reads[MarketQuote] = (
    (__ \ "symbol").read[String] and
      (__ \ "name").readNullable[String] and
      (__ \ "lastTrade").readNullable[Double] and
      (__ \ "close").readNullable[Double])(MarketQuote.apply _)

  implicit val marketQuoteWrites: Writes[MarketQuote] = (
    (__ \ "symbol").write[String] and
      (__ \ "name").writeNullable[String] and
      (__ \ "lastTrade").writeNullable[Double] and
      (__ \ "close").writeNullable[Double])(unlift(MarketQuote.unapply))

  implicit object MarketQuoteReader extends BSONDocumentReader[MarketQuote] {
    override def read(doc: BSONDocument) = MarketQuote(
      doc.getAs[String]("symbol").get,
      doc.getAs[String]("name"),
      doc.getAs[Double]("lastTrade"),
      doc.getAs[Double]("close")
    )
  }

  implicit object MarketQuoteWriter extends BSONDocumentWriter[MarketQuote] {
    override def write(quote: MarketQuote) = BSONDocument(
      "symbol" -> quote.symbol,
      "name" -> quote.name,
      "lastTrade" -> quote.lastTrade,
      "close" -> quote.close
    )
  }

}