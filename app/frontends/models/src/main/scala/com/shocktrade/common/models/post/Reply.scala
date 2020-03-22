package com.shocktrade.common.models.post

import scala.scalajs.js

/**
 * Represents a reply
 * @author lawrence.daniels@gmail.com
 * @see [[ReplyLikes]]
 */
class Reply(var replyID: js.UndefOr[String] = js.undefined,
            var text: js.UndefOr[String] = js.undefined,
            var userID: js.UndefOr[String] = js.undefined,
            var creationTime: js.UndefOr[js.Date] = js.undefined,
            var lastUpdateTime: js.UndefOr[js.Date] = js.undefined) extends js.Object {

  // UI-only indicators
  var likeLoading: js.UndefOr[Boolean] = js.undefined

}

/**
 * Reply Companion
 * @author lawrence.daniels@gmail.com
 */
object Reply {

}