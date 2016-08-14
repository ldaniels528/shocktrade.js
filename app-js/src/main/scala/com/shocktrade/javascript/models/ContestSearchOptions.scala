package com.shocktrade.javascript.models

import org.scalajs.nodejs.util.ScalaJsHelper._

import scala.scalajs.js

/**
 * Contest Search Options
 */
@js.native
trait ContestSearchOptions extends js.Object {
  var activeOnly: Boolean
  var available: Boolean
  var friendsOnly: Boolean
  var levelCap: String
  var levelCapAllowed: Boolean
  var perksAllowed: Boolean
  var robotsAllowed: Boolean
}

/**
 * Contest Search Options Singleton
 */
object ContestSearchOptions {

  def apply() = {
    val options = New[ContestSearchOptions]
    options.activeOnly = false
    options.available = false
    options.friendsOnly = false
    options.levelCap = "1"
    options.levelCapAllowed = false
    options.perksAllowed = false
    options.robotsAllowed = false
    options
  }

}