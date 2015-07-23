package com.shocktrade.models.quote

import play.api.libs.json._
import reactivemongo.bson.{BSONDocument, BSONDocumentReader, BSONDocumentWriter}

/**
 * Auto-Complete Quote
 */
case class AutoCompleteQuote(symbol: String,
                             name: Option[String] = None,
                             exchange: Option[String] = None,
                             assetType: Option[String] = None,
                             icon: Option[String] = None)

/**
 * Auto-Complete Quote Singleton
 */
object AutoCompleteQuote {
  implicit val autoCompleteQuoteReads = Json.reads[AutoCompleteQuote]
  implicit val autoCompleteQuoteWrites = Json.writes[AutoCompleteQuote]

  implicit object AutoCompleteQuoteReader extends BSONDocumentReader[AutoCompleteQuote] {
    def read(doc: BSONDocument) = AutoCompleteQuote(
      doc.getAs[String]("symbol").get,
      doc.getAs[String]("name"),
      doc.getAs[String]("exchange"),
      doc.getAs[String]("assetType"),
      doc.getAs[String]("icon")
    )
  }

  implicit object AutoCompleteQuoteWriter extends BSONDocumentWriter[AutoCompleteQuote] {
    def write(quote: AutoCompleteQuote) = BSONDocument(
      "symbol" -> quote.symbol,
      "name" -> quote.name,
      "exchange" -> quote.exchange,
      "assetType" -> quote.assetType,
      "icon" -> quote.icon
    )
  }

}