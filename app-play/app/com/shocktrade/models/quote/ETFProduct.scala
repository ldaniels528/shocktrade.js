package com.shocktrade.models.quote

import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json.{Json, Reads, Writes, __}
import reactivemongo.bson.{BSONDocument, BSONDocumentReader, _}

/**
 * Represents an ETF Product
 */
case class ETFProduct(name: String, symbol: String, netAssetsPct: Double)

/**
 * ETF Product Singleton
 */
object ETFProduct {
  implicit val etfProductReads = Json.reads[ETFProduct]
  implicit val etfProductWrites = Json.writes[ETFProduct]

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