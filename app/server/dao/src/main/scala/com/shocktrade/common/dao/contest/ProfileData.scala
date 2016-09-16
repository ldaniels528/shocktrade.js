package com.shocktrade.common.dao.contest

import com.shocktrade.common.models.ProfileLike
import com.shocktrade.common.models.ProfileLike.QuoteFilter
import org.scalajs.nodejs.mongodb.ObjectID

import scala.scalajs.js

/**
  * Represents a Profile data model
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
@js.native
trait ProfileData extends ProfileLike {
  var _id: js.UndefOr[ObjectID] = js.native
  var facebookID: js.UndefOr[String] = js.native

  var name: js.UndefOr[String] = js.native
  var country: js.UndefOr[String] = js.native
  var level: js.UndefOr[Int] = js.native
  var rep: js.UndefOr[Int] = js.native
  var netWorth: js.UndefOr[Double] = js.native
  var totalXP: js.UndefOr[Int] = js.native

  var favoriteSymbols: js.UndefOr[js.Array[String]] = js.native
  var recentSymbols: js.UndefOr[js.Array[String]] = js.native
  var filters: js.UndefOr[js.Array[QuoteFilter]] = js.native
  var friends: js.UndefOr[js.Array[String]] = js.native
  var accomplishments: js.UndefOr[js.Array[String]] = js.native
  var acquaintances: js.UndefOr[js.Array[String]] = js.native
  var lastLoginTime: js.UndefOr[js.Date] = js.native

}
