package com.shocktrade.common.models.post

import com.shocktrade.common.models.user.UserLike

import scala.scalajs.js
import scala.scalajs.js.annotation.ScalaJSDefined

/**
  * Represents a reply
  * @author lawrence.daniels@gmail.com
  * @see [[ReplyLikes]]
  */
class Reply extends js.Object {
  var _id: js.UndefOr[String] = js.undefined
  var text: js.UndefOr[String] = js.undefined
  var submitter: js.UndefOr[UserLike] = js.undefined
  var creationTime: js.UndefOr[js.Date] = js.undefined
  var lastUpdateTime: js.UndefOr[js.Date] = js.undefined

  // UI-only indicators
  var likeLoading: js.UndefOr[Boolean] = js.undefined
}

/**
  * Reply Companion
  * @author lawrence.daniels@gmail.com
  */
object Reply {

  def apply(text: String, submitter: UserLike): Reply = {
    val reply = new Reply()
    reply.text = text
    reply.submitter = submitter
    reply.creationTime = new js.Date()
    reply
  }

}