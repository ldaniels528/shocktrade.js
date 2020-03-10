package com.shocktrade.common.models.post

import scala.scalajs.js

/**
 * Represents the Like action for a Reply
 * <b>NOTE</b>: due to a MongoDB restriction for deeply nested objects,
 * likes could not be included in the [[Reply reply class]]
 * @author lawrence.daniels@gmail.com
 */
@js.native
trait ReplyLikes extends js.Object {
  var _id: js.UndefOr[String] = js.native
  var likes: js.UndefOr[Int] = js.native
  var likedBy: js.UndefOr[js.Array[String]] = js.native
}