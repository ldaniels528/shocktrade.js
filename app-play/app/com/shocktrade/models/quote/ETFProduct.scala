package com.shocktrade.models.quote

import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json.{Reads, Writes, __}
import reactivemongo.bson.{BSONDocument, BSONDocumentReader, _}

/**
 * Represents an ETF Product
 */
case class ETFProduct(name: String, symbol: String, netAssetsPct: Double)

/**
 * ETF Product Singleton
 */
object ETFProduct {

  implicit val etfProductReads: Reads[ETFProduct] = (
    (__ \ "name").read[String] and
      (__ \ "symbol").read[String] and
      (__ \ "netAssetsPct").read[Double])(ETFProduct.apply _)

  implicit val etfProductWrites: Writes[ETFProduct] = (
    (__ \ "name").write[String] and
      (__ \ "symbol").write[String] and
      (__ \ "netAssetsPct").write[Double])(unlift(ETFProduct.unapply))

  implicit object ETFProductReader extends BSONDocumentReader[ETFProduct] {
    def read(doc: BSONDocument) = ETFProduct(
      doc.getAs[String]("name").get,
      doc.getAs[String]("symbol").get,
      doc.getAs[Double]("netAssetsPct").get
    )
  }

  implicit object ETFProductWriter extends BSONDocumentWriter[ETFProduct] {
    def write(product: ETFProduct) = BSONDocument(
      "name" -> product.name,
      "symbol" -> product.symbol,
      "netAssetsPct" -> product.netAssetsPct
    )
  }

}