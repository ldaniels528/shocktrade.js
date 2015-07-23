package com.shocktrade.models.quote

import play.api.libs.json._
import reactivemongo.bson.{BSONDocument, BSONDocumentReader, BSONDocumentWriter}

/**
 * Represents the valuation attributes of the quote
 * @author lawrence.daniels@gmail.com
 */
case class Valuation(beta: Option[Double] = None, target1Yr: Option[Double] = None)

/**
 * Ask/Bid Singleton
 * @author lawrence.daniels@gmail.com
 */
object Valuation {
  implicit val valuationReads = Json.reads[Valuation]
  implicit val valuationWrites = Json.writes[Valuation]

  implicit object ValuationReader extends BSONDocumentReader[Valuation] {
    def read(doc: BSONDocument) = Valuation(
      doc.getAs[Double]("beta"),
      doc.getAs[Double]("target1Yr")
    )
  }

  implicit object ValuationWriter extends BSONDocumentWriter[Valuation] {
    def write(quote: Valuation) = BSONDocument(
      "beta" -> quote.beta,
      "target1Yr" -> quote.target1Yr
    )
  }

}
