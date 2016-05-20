package com.shocktrade.javascript.models

import com.github.ldaniels528.meansjs.util.ScalaJsHelper._

import scala.scalajs.js

/**
 * Player Information
  * @author lawrence.daniels@gmail.com
 */
@js.native
trait PlayerInfo extends js.Object {
  var _id: js.UndefOr[BSONObjectID]
  var facebookID: String
  var name: String
}

/**
 * Player Information Singleton
 */
object PlayerInfo {

  def apply(id: BSONObjectID, facebookID: String, name: String) = {
    val info = New[PlayerInfo]
    info._id = id
    info.facebookID = facebookID
    info.name = name
    info
  }

}
