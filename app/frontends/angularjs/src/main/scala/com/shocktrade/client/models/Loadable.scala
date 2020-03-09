package com.shocktrade.client.models

import scala.scalajs.js

/**
 * Represents a loadable resource
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
trait Loadable {
  ref: js.Object =>

  var loading: Boolean = false
  var deleting: Boolean = false
  var joining: Boolean = false
  var quitting: Boolean = false
  var starting: Boolean = false

}
