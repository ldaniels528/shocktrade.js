package com.shocktrade.common.models.post

import scala.scalajs.js

/**
  * Represents a Post-like model
  * @author lawrence.daniels@gmail.com
  */
trait PostLike extends js.Object {

  def text: js.UndefOr[String]

  def userID: js.UndefOr[String]

  def summary: js.UndefOr[SharedContent]

  def likes: js.UndefOr[Int]

  def likedBy: js.UndefOr[js.Array[String]]

  def creationTime: js.Date

  def lastUpdateTime: js.Date

  // collections
  def attachments: js.UndefOr[js.Array[String]]

  def comments: js.UndefOr[js.Array[Comment]]

  def replyLikes: js.UndefOr[js.Array[ReplyLikes]]

  def tags: js.UndefOr[js.Array[String]]

}