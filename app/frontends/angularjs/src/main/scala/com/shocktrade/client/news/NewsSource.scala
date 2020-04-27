package com.shocktrade.client.news

import scala.scalajs.js

/**
 * News Source
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
@js.native
trait NewsSource extends js.Object {
  var rssFeedID: js.UndefOr[String] = js.native
}
