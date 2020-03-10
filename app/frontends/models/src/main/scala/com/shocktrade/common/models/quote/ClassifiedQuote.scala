package com.shocktrade.common.models.quote

import scala.scalajs.js

/**
 * Classified Quote
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
@js.native
trait ClassifiedQuote extends js.Object {
  var assetClass: js.UndefOr[String] = js.native
  var assetType: js.UndefOr[String] = js.native

}
