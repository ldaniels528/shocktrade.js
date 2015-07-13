package com.shocktrade.javascript.models

import com.shocktrade.javascript.ScalaJsHelper._
import scala.scalajs.js

/**
 * Online Player State
 */
trait OnlinePlayerState extends js.Object {
  var connected: Boolean = js.native

}

/**
 * Online Player State Singleton
 */
object OnlinePlayerState {

  def apply(connected: Boolean = false) = {
    val state = makeNew[OnlinePlayerState]
    state.connected = connected
    state
  }

}