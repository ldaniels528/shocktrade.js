package com.shocktrade.models.quote

import play.api.libs.json._
import reactivemongo.bson.Macros

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

  implicit val AskBidFormat = Json.format[AskBid]

  implicit val AskBidHandler = Macros.handler[AskBid]

}
