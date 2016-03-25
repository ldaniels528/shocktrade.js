package com.shocktrade.models.quote

import play.api.libs.json.Json
import reactivemongo.bson.Macros

/**
  * Change Quote Model
  * @author lawrence.daniels@gmail.com
  */
case class ChangeQuote(symbol: Option[String],
                       exchange: Option[String],
                       name: Option[String],
                       lastTrade: Option[Double],
                       changePct: Option[Double],
                       volume: Option[Long],
                       sector: Option[String],
                       industry: Option[String])

/**
  * Change Quote Companion Object
  * @author lawrence.daniels@gmail.com
  */
object ChangeQuote {

  implicit val ChangeQuoteFormat = Json.format[ChangeQuote]

  implicit val ChangeQuoteHandler = Macros.handler[ChangeQuote]

}