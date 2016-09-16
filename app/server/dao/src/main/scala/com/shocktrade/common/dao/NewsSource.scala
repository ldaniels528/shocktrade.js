package com.shocktrade.common.dao

import scala.scalajs.js

/**
  * News Source
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
@js.native
trait NewsSource extends js.Object {
  var name: String = js.native
  var url: String = js.native
  var priority: Int = js.native
}
