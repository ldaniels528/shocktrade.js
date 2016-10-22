package com.shocktrade.server.dao.users

import com.shocktrade.common.models.user.ProfileLike
import com.shocktrade.common.models.user.ProfileLike.QuoteFilter
import org.scalajs.nodejs.mongodb.ObjectID

import scala.scalajs.js
import scala.scalajs.js.annotation.ScalaJSDefined

/**
  * Represents a User Profile data model
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
@ScalaJSDefined
class ProfileData(var _id: js.UndefOr[ObjectID] = js.undefined,
                  var facebookID: js.UndefOr[String] = js.undefined,
                  var name: js.UndefOr[String] = js.undefined,
                  var country: js.UndefOr[String] = js.undefined,
                  var level: js.UndefOr[Int] = js.undefined,
                  var rep: js.UndefOr[Int] = js.undefined,
                  var netWorth: js.UndefOr[Double] = js.undefined,
                  var totalXP: js.UndefOr[Int] = js.undefined,
                  var favoriteSymbols: js.UndefOr[js.Array[String]] = js.undefined,
                  var recentSymbols: js.UndefOr[js.Array[String]] = js.undefined,
                  var filters: js.UndefOr[js.Array[QuoteFilter]] = js.undefined,
                  var followers: js.UndefOr[js.Array[String]] = js.undefined,
                  var friends: js.UndefOr[js.Array[String]] = js.undefined,
                  var accomplishments: js.UndefOr[js.Array[String]] = js.undefined,
                  var acquaintances: js.UndefOr[js.Array[String]] = js.undefined,
                  var lastLoginTime: js.UndefOr[js.Date] = js.undefined) extends ProfileLike with UserData