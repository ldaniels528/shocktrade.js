package com.shocktrade.processors.actors

import play.api.libs.json.Json
import reactivemongo.bson.Macros

/**
  * Represents a missing CIK information request
  * @author lawrence.daniels@gmail.com
  */
case class MissingCik(symbol: String, name: String)

/**
  * Missing Cik Singleton
  * @author lawrence.daniels@gmail.com
  */
object MissingCik {

  implicit val MissingCikFormat = Json.format[MissingCik]

  implicit val MissingCikHandler = Macros.handler[MissingCik]

}