package com.shocktrade.models.quote

import play.api.libs.json._
import reactivemongo.bson.Macros

/**
  * Auto-Complete Quote
  * @author lawrence.daniels@gmail.com
  */
case class AutoCompleteQuote(symbol: String,
                             name: Option[String] = None,
                             exchange: Option[String] = None,
                             assetType: Option[String] = None,
                             icon: Option[String] = None)

/**
  * Auto-Complete Quote Singleton
  * @author lawrence.daniels@gmail.com
  */
object AutoCompleteQuote {

  implicit val AutoCompleteQuoteFormat = Json.format[AutoCompleteQuote]

  implicit val AutoCompleteQuoteHandler = Macros.handler[AutoCompleteQuote]

}