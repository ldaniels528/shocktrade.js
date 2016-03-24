package com.shocktrade.javascript.models

import scala.scalajs.js

/**
  * News Feed Item
  * @author lawrence.daniels@gmail.com
  */
@js.native
trait NewsFeedItem extends js.Object {
  var quotes: js.Array[NewsQuote]

}
