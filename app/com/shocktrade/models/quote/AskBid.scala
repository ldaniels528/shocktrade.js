package com.shocktrade.models.quote

import play.api.libs.functional.syntax._
import play.api.libs.json._
import reactivemongo.bson.{BSONDocument, BSONDocumentReader, BSONDocumentWriter}

/**
 * Represents the ask/bid information for a stock
 * @author lawrence.daniels@gmail.com
 */
case class AskBid(ask: Option[Double] = None,
                  askSize: Option[Int] = None,
                  bid: Option[Double] = None,
                  bidSize: Option[Int] = None)

/**
 * Ask/Bid Singleton
 * @author lawrence.daniels@gmail.com
 */
object AskBid {

  implicit val askBidReads: Reads[AskBid] = (
    (__ \ "ask").readNullable[Double] and
      (__ \ "askSize").readNullable[Int] and
      (__ \ "bid").readNullable[Double] and
      (__ \ "bidSize").readNullable[Int])(AskBid.apply _)

  implicit val askBidWrites: Writes[AskBid] = (
    (__ \ "ask").writeNullable[Double] and
      (__ \ "askSize").writeNullable[Int] and
      (__ \ "bid").writeNullable[Double] and
      (__ \ "bidSize").writeNullable[Int])(unlift(AskBid.unapply))

  implicit object AskBidReader extends BSONDocumentReader[AskBid] {
    def read(doc: BSONDocument) = AskBid(
      doc.getAs[Double]("ask"),
      doc.getAs[Int]("askSize"),
      doc.getAs[Double]("bid"),
      doc.getAs[Int]("bidSize")
    )
  }

  implicit object AskBidWriter extends BSONDocumentWriter[AskBid] {
    def write(quote: AskBid) = BSONDocument(
      "ask" -> quote.ask,
      "askSize" -> quote.askSize,
      "bid" -> quote.bid,
      "bidSize" -> quote.bidSize
    )
  }

}
