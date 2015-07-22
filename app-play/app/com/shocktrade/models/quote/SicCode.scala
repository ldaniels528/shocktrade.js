package com.shocktrade.models.quote

import play.api.libs.functional.syntax._
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

  implicit val sicCodeReads: Reads[SicCode] = (
    (__ \ 'sicNumber).read[Int] and
      (__ \ 'description).read[String])(SicCode.apply _)

  implicit val sicCodeWrites: Writes[SicCode] = (
    (__ \ 'sicNumber).write[Int] and
      (__ \ 'description).write[String])(unlift(SicCode.unapply))

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
