package com.shocktrade.models.quote

import play.api.libs.functional.syntax._
import play.api.libs.json._
import reactivemongo.bson.{BSONDocument, BSONDocumentReader, BSONDocumentWriter}

/**
 * Product Quote
 */
case class ProductQuote(symbol: String,
                        name: Option[String] = None,
                        exchange: Option[String] = None,
                        lastTrade: Option[Double] = None,
                        change: Option[Double] = None,
                        changePct: Option[Double] = None,
                        spread: Option[Double] = None,
                        volume: Option[Long] = None,
                        active: Option[Boolean] = None)

/**
 * Product Quote Singleton
 */
object ProductQuote {
  val Fields = Seq("name", "symbol", "exchange", "lastTrade", "change", "changePct", "spread", "volume")

  implicit val productQuoteReads: Reads[ProductQuote] = {
    ((__ \ "symbol").read[String] and
      (__ \ "name").readNullable[String] and
      (__ \ "exchange").readNullable[String] and
      (__ \ "lastTrade").readNullable[Double] and
      (__ \ "change").readNullable[Double] and
      (__ \ "changePct").readNullable[Double] and
      (__ \ "spread").readNullable[Double] and
      (__ \ "volume").readNullable[Long] and
      (__ \ "active").readNullable[Boolean])(ProductQuote.apply _)
  }

  implicit val productQuoteWrites: Writes[ProductQuote] = (
    (__ \ "symbol").write[String] and
      (__ \ "name").writeNullable[String] and
      (__ \ "exchange").writeNullable[String] and
      (__ \ "lastTrade").writeNullable[Double] and
      (__ \ "change").writeNullable[Double] and
      (__ \ "changePct").writeNullable[Double] and
      (__ \ "spread").writeNullable[Double] and
      (__ \ "volume").writeNullable[Long] and
      (__ \ "active").writeNullable[Boolean])(unlift(ProductQuote.unapply))

  implicit object ProductQuoteReader extends BSONDocumentReader[ProductQuote] {
    def read(doc: BSONDocument) = ProductQuote(
      doc.getAs[String]("symbol").get,
      doc.getAs[String]("name"),
      doc.getAs[String]("exchange"),
      doc.getAs[Double]("lastTrade"),
      doc.getAs[Double]("change"),
      doc.getAs[Double]("changePct"),
      doc.getAs[Double]("spread"),
      doc.getAs[Long]("volume"),
      doc.getAs[Boolean]("active")
    )
  }

  implicit object ProductQuoteWriter extends BSONDocumentWriter[ProductQuote] {
    def write(quote: ProductQuote) = BSONDocument(
      "symbol" -> quote.symbol,
      "name" -> quote.name,
      "exchange" -> quote.exchange,
      "lastTrade" -> quote.lastTrade,
      "change" -> quote.change,
      "changePct" -> quote.changePct,
      "spread" -> quote.spread,
      "volume" -> quote.volume,
      "active" -> quote.active
    )
  }

}
