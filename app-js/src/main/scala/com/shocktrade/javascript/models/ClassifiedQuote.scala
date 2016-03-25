package com.shocktrade.javascript.models

import scala.scalajs.js

/**
  * Classified Quote
  * @author lawrence.daniels@gmail.com
  */
@js.native
trait ClassifiedQuote extends js.Object {
  var assetClass: js.UndefOr[String]
  var assetType: js.UndefOr[String]

}
