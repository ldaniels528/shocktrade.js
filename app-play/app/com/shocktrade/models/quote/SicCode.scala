package com.shocktrade.models.quote

import play.api.libs.json._
import reactivemongo.bson.Macros

/**
  * Represents a SIC Code
  * @author lawrence.daniels@gmail.com
  */
case class SicCode(sicNumber: Int, description: String)

/**
  * SIC Code Singleton
  * @author lawrence.daniels@gmail.com
  */
object SicCode {

  implicit val SicCodeFormat = Json.format[SicCode]

  implicit val SicCodeHandler = Macros.handler[SicCode]

}
