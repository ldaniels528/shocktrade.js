package com.shocktrade.controllers

import play.api.libs.json.Json.{obj => JS}
import play.api.libs.json.Json._

/**
 * JSON Error Mapping Functions
 * @author lawrence.daniels@gmail.com
 */
trait ErrorHandler {

   def createError(message: String) = {
    JS("error" -> message)
  }

   def createError(e: Throwable) = {
    JS("error" -> e.getMessage)
  }

}
