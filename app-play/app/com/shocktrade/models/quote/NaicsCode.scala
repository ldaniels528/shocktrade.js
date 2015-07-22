package com.shocktrade.models.quote

import play.api.libs.functional.syntax._
import play.api.libs.json._
import reactivemongo.bson.{BSONDocumentWriter, BSONDocument, BSONDocumentReader}

/**
 * Represents a NAICS Code
 */
case class NaicsCode(naicsNumber: Int, description: String)

/**
 * NAICS Code Singleton
 */
object NaicsCode {

  implicit val naicsCodeReads: Reads[NaicsCode] = (
    (__ \ 'naicsNumber).read[Int] and
      (__ \ 'description).read[String])(NaicsCode.apply _)

  implicit val naicsCodeWrites: Writes[NaicsCode] = (
    (__ \ 'naicsNumber).write[Int] and
      (__ \ 'description).write[String])(unlift(NaicsCode.unapply))

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