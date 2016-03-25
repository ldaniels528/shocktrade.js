package com.shocktrade.models.quote

import play.api.libs.json._
import reactivemongo.bson.Macros

/**
  * Represents a NAICS Code Model
  * @author lawrence.daniels@gmail.com
  */
case class NaicsCode(naicsNumber: Int, description: String)

/**
  * NAICS Code Singleton
  * @author lawrence.daniels@gmail.com
  */
object NaicsCode {

  implicit val NaicsCodeFormat = Json.format[NaicsCode]

  implicit val NaicsCodeHandler = Macros.handler[NaicsCode]

}