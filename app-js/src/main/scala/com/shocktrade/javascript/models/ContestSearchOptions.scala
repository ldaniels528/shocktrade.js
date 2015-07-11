package com.shocktrade.javascript.models

import com.shocktrade.javascript.ScalaJsHelper._

import scala.scalajs.js

/**
 * Contest Search Options
 */
trait ContestSearchOptions extends js.Object {
  var activeOnly: Boolean = js.native
  var available: Boolean = js.native
  var friendsOnly: Boolean = js.native
  var levelCap: String = js.native
  var levelCapAllowed: Boolean = js.native
  var perksAllowed: Boolean = js.native
  var robotsAllowed: Boolean = js.native
}

/**
 * Contest Search Options Singleton
 */
object ContestSearchOptions {

  def apply() = {
    val options = makeNew[ContestSearchOptions]
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