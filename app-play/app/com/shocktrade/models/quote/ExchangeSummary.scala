package com.shocktrade.models.quote

import play.api.libs.json.Json
import reactivemongo.bson.Macros

/**
  * Exchange Summary
  * @author lawrence.daniels@gmail.com
  */
case class ExchangeSummary(exchange: String, count: Long)

/**
  * Exchange Summary Singleton
  * @author lawrence.daniels@gmail.com
  */
object ExchangeSummary {

  implicit val ExchangeSummaryFormat = Json.format[ExchangeSummary]

  implicit val ExchangeSummaryHandler = Macros.handler[ExchangeSummary]

}