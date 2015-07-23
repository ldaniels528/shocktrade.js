package com.shocktrade.models.quote

import play.api.libs.json._
import reactivemongo.bson.{BSONDocument, BSONDocumentReader, BSONDocumentWriter}

/**
 * Represents a SIC Code
 */
case class SicCode(sicNumber: Int, description: String)

/**
 * SIC Code Singleton
 */
object SicCode {
  implicit val sicCodeReads = Json.reads[SicCode]
  implicit val sicCodeWrites = Json.writes[SicCode]

  implicit object SicCodeReader extends BSONDocumentReader[SicCode] {
    override def read(doc: BSONDocument) = SicCode(
      doc.getAs[Int]("sicNumber").getOrElse(-1),
      doc.getAs[String]("description").orNull
    )
  }

  implicit object SicCodeWriter extends BSONDocumentWriter[SicCode] {
    override def write(quote: SicCode) = BSONDocument(
      "sicNumber" -> quote.sicNumber,
      "description" -> quote.description
    )
  }

}
