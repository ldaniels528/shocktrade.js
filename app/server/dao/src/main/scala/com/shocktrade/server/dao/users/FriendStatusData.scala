package com.shocktrade.server.dao.users

import scala.scalajs.js
import scala.scalajs.js.annotation.ScalaJSDefined

/**
  * Represents Friend Status Data
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
@ScalaJSDefined
trait FriendStatusData extends js.Object {
  var facebookID: js.UndefOr[String]
  var name: js.UndefOr[String]
  var status: js.UndefOr[String]
  var gamesCompleted: js.UndefOr[Int]
  var gamesCreated: js.UndefOr[Int]
  var gamesDeleted: js.UndefOr[Int]
  var lastLoginTime: js.UndefOr[js.Date]
}

/**
  * User Status Data
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
object FriendStatusData {
  val Fields = js.Array("facebookID", "name", "gamesCompleted", "gamesCreated", "gamesDeleted", "lastLoginTime")

}