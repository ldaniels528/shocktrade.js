package com.shocktrade.models.quote

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
  implicit val askBidReads = Json.reads[AskBid]
  implicit val askBidWrites = Json.writes[AskBid]

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
