package com.shocktrade.javascript.models.contest

import scala.scalajs.js

/**
  * Participant Ranking
  * @author lawrence.daniels@gmail.com
  */
@js.native
trait ParticipantRanking extends js.Object {
  var _id: js.UndefOr[String] = js.native
  var facebookID: js.UndefOr[String] = js.native
  var name: js.UndefOr[String] = js.native
  var rank: js.UndefOr[String] = js.native
  var totalEquity: js.UndefOr[Double] = js.native
  var gainLoss: js.UndefOr[Double] = js.native

}