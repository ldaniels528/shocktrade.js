package com.shocktrade.models.quote

import play.api.libs.json._
import reactivemongo.bson.{BSONDocument, BSONDocumentReader, BSONDocumentWriter}

/**
 * Represents a NAICS Code
 */
case class NaicsCode(naicsNumber: Int, description: String)

/**
 * NAICS Code Singleton
 */
object NaicsCode {
  implicit val naicsCodeReads = Json.reads[NaicsCode]
  implicit val naicsCodeWrites = Json.writes[NaicsCode]

  implicit object NaicsCodeReader extends BSONDocumentReader[NaicsCode] {
    override def read(doc: BSONDocument) = NaicsCode(
      doc.getAs[Int]("naicsNumber").getOrElse(-1),
      doc.getAs[String]("description").orNull
    )
  }

  implicit object NaicsCodeWriter extends BSONDocumentWriter[NaicsCode] {
    override def write(quote: NaicsCode) = BSONDocument(
      "naicsNumber" -> quote.naicsNumber,
      "description" -> quote.description
    )
  }

}