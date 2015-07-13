package com.shocktrade.javascript.models

import com.shocktrade.javascript.ScalaJsHelper._

import scala.scalajs.js

/**
 * Player Information
 */
trait PlayerInfo extends js.Object {
  var id: String = js.native
  var facebookID: String = js.native
  var name: String = js.native
}

/**
 * Player Information Singleton
 */
object PlayerInfo {

  def apply(id: String, facebookID: String, name: String) = {
    val info = makeNew[PlayerInfo]
    info.id = id
    info.facebookID = facebookID
    info.name = name
    info
  }

}
