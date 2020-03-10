package com.shocktrade.common.models.post

import scala.scalajs.js

/**
 * Represents Shared Content; usually posted from a news site, etc.
 * @author lawrence.daniels@gmail.com
 */
@js.native
trait SharedContent extends js.Object {
  var author: js.UndefOr[String] = js.native
  var description: js.UndefOr[String] = js.native
  var locale: js.UndefOr[String] = js.native
  var publishedTime: js.UndefOr[js.Date] = js.native
  var section: js.UndefOr[String] = js.native
  var source: js.UndefOr[String] = js.native
  var tags: js.UndefOr[js.Array[String]] = js.native
  var title: js.UndefOr[String] = js.native
  var thumbnailUrl: js.UndefOr[String] = js.native
  var updatedTime: js.UndefOr[js.Date] = js.native
  var url: js.UndefOr[String] = js.native

}

