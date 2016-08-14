package com.shocktrade.javascript.models.contest

import scala.scalajs.js

@js.native
trait Message extends js.Object {
  var _id: js.UndefOr[String] = js.native
  var sender: js.UndefOr[PlayerRef] = js.native
  var text: js.UndefOr[String] = js.native
  var recipient: js.UndefOr[PlayerRef] = js.native
  var sentTime: js.UndefOr[js.Date] = js.native
}
