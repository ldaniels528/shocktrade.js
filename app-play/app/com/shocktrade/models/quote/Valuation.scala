package com.shocktrade.models.quote

import play.api.libs.json._
import reactivemongo.bson.Macros

/**
  * Represents the valuation attributes of the quote
  * @author lawrence.daniels@gmail.com
  */
case class Valuation(beta: Option[Double] = None, target1Yr: Option[Double] = None)

/**
  * Valuation Singleton
  * @author lawrence.daniels@gmail.com
  */
object Valuation {

  implicit val ValuationFormat = Json.format[Valuation]

  implicit val ValuationHandler = Macros.handler[Valuation]

}
