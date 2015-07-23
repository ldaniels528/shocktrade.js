package com.shocktrade.models.quote

import play.api.libs.json.Json
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

  implicit val marketQuoteReads = Json.reads[MarketQuote]
  implicit val marketQuoteWrites = Json.writes[MarketQuote]

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