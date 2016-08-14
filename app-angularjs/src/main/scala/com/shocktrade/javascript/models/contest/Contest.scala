package com.shocktrade.javascript.models.contest



import scala.scalajs.js

/**
  * Contest Model
  * @author lawrence.daniels@gmail.com
  */
@js.native
trait Contest extends ContestLike {
  var _id: js.UndefOr[String] = js.native
  var rankings: js.UndefOr[Rankings] = js.native
  var messages: js.UndefOr[js.Array[Message]] = js.native

  // administrative fields
  var error: js.UndefOr[String]
  var rankingsHidden: js.UndefOr[Boolean]
  var deleting: Boolean
  var joining: Boolean
  var quitting: Boolean
  var starting: Boolean
}

/**
  * Contest Model Singleton
  * @author lawrence.daniels@gmail.com
  */
object Contest {
  val MaxPlayers = 24

}








