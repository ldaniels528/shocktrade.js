package com.shocktrade.common.models.user

import scala.scalajs.js
import scala.scalajs.js.annotation.ScalaJSDefined

/**
  * Represents a Friend Status Model
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
@ScalaJSDefined
class FriendStatus(var facebookID: js.UndefOr[String],
                   var name: js.UndefOr[String],
                   var status: js.UndefOr[String],
                   var gamesCompleted: js.UndefOr[Int] = js.undefined,
                   var gamesCreated: js.UndefOr[Int] = js.undefined,
                   var gamesDeleted: js.UndefOr[Int] = js.undefined,
                   var lastLoginTime: js.UndefOr[js.Date] = js.undefined) extends js.Object