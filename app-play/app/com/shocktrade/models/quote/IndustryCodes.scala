package com.shocktrade.models.quote

import play.api.libs.json._
import reactivemongo.bson.{BSONDocument, BSONDocumentReader, BSONDocumentWriter}

/**
 * Represents the classification information of a stock quote
 * @author lawrence.daniels@gmail.com
 */
case class IndustryCodes(sector: Option[String] = None,
                         industry: Option[String] = None,
                         sicNumber: Option[Int] = None,
                         naicsNumber: Option[Int] = None)

/**
 * Industry Codes Singleton
 * @author lawrence.daniels@gmail.com
 */
object IndustryCodes {

  implicit val industryCodesReads = Json.reads[IndustryCodes]
  implicit val industryCodesWrites = Json.writes[IndustryCodes]

  implicit object IndustryCodesReader extends BSONDocumentReader[IndustryCodes] {
    def read(doc: BSONDocument) = IndustryCodes(
      doc.getAs[String]("sector"),
      doc.getAs[String]("industry"),
      doc.getAs[Int]("sicNumber"),
      doc.getAs[Int]("naicsNumber")
    )
  }

  implicit object IndustryCodesWriter extends BSONDocumentWriter[IndustryCodes] {
    def write(quote: IndustryCodes) = BSONDocument(
      "sector" -> quote.sector,
      "industry" -> quote.industry,
      "sicNumber" -> quote.sicNumber,
      "naicsNumber" -> quote.naicsNumber
    )
  }

}

