package com.shocktrade.common.models.post

import scala.scalajs.js

/**
 * Represents the Like action for a Reply
 * <b>NOTE</b>: due to a MongoDB restriction for deeply nested objects,
 * likes could not be included in the [[Reply reply class]]
 * @author lawrence.daniels@gmail.com
 */
class ReplyLikes(var replyID: js.UndefOr[String] = js.undefined,
                 var likes: js.UndefOr[Int] = js.undefined,
                 var likedBy: js.UndefOr[js.Array[String]] = js.undefined) extends js.Object