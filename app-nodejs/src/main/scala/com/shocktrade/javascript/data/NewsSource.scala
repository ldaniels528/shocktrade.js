package com.shocktrade.javascript.data

import scala.scalajs.js

/**
  * News Source
  * @author lawrence.daniels@gmail.com
  */
@js.native
trait NewsSource extends js.Object {
  var name: String = js.native
  var url: String = js.native
  var priority: Int = js.native
}
