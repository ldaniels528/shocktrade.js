package com.shocktrade.javascript.models

import com.github.ldaniels528.meansjs.util.ScalaJsHelper._

import scala.scalajs.js

/**
  * Online Player State
  * @author lawrence.daniels@gmail.com
  */
@js.native
trait OnlinePlayerState extends js.Object {
  var connected: Boolean = js.native

}

/**
  * Online Player State Singleton
  */
object OnlinePlayerState {

  def apply(connected: Boolean = false) = {
    val state = New[OnlinePlayerState]
    state.connected = connected
    state
  }

}