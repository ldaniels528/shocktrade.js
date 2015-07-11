package com.shocktrade.javascript.models

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
    val state = js.Object.asInstanceOf[OnlinePlayerState]
    state.connected = connected
    state
  }

}