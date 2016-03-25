package com.shocktrade.models.quote

import play.api.libs.json._
import reactivemongo.bson.Macros

/**
  * Represents a Stock Quote Filter
  * @author lawrence.daniels@gmail.com
  */
case class QuoteFilter(changeMin: Option[Double] = None,
                       changeMax: Option[Double] = None,
                       spreadMin: Option[Double] = None,
                       spreadMax: Option[Double] = None,
                       marketCapMin: Option[Double] = None,
                       marketCapMax: Option[Double] = None,
                       priceMin: Option[Double] = None,
                       priceMax: Option[Double] = None,
                       volumeMin: Option[Long] = None,
                       volumeMax: Option[Long] = None,
                       maxResults: Option[Int] = None)

/**
  * Quote Filter Singleton
  * @author lawrence.daniels@gmail.com
  */
object QuoteFilter {

  implicit val QuoteFilterFormat = Json.format[QuoteFilter]

  implicit val QuoteFilterHandler = Macros.handler[QuoteFilter]

}