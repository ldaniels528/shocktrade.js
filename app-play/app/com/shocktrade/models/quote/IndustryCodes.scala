package com.shocktrade.models.quote

import play.api.libs.json._
import reactivemongo.bson.Macros

/**
  * Represents the classification information of a stock quote
  * @author lawrence.daniels@gmail.com
  */
case class IndustryCodes(sector: Option[String] = None,
                         industry: Option[String] = None,
                         sicNumber: Option[Int] = None,
                         naicsNumber: Option[Int] = None)

/**
  * Industry Codes Singleton
  * @author lawrence.daniels@gmail.com
  */
object IndustryCodes {

  implicit val IndustryCodesFormat = Json.format[IndustryCodes]

  implicit val IndustryCodesHandler = Macros.handler[IndustryCodes]

}

