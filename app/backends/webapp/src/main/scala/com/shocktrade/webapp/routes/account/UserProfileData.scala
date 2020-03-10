package com.shocktrade.webapp.routes.account

import com.shocktrade.common.models.user.UserProfileLike

import scala.scalajs.js

/**
 * Represents a User Profile data model
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
class UserProfileData(var userID: js.UndefOr[String],
                      var username: js.UndefOr[String],
                      var country: js.UndefOr[String],
                      var level: js.UndefOr[Int],
                      var rep: js.UndefOr[Int],
                      var netWorth: js.UndefOr[Double],
                      var wallet: js.UndefOr[Double],
                      var totalXP: js.UndefOr[Int],
                      var favoriteSymbols: js.UndefOr[js.Array[String]],
                      var recentSymbols: js.UndefOr[js.Array[String]],
                      var followers: js.UndefOr[js.Array[String]],
                      var friends: js.UndefOr[js.Array[String]],
                      var awards: js.UndefOr[js.Array[String]],
                      var lastLoginTime: js.UndefOr[js.Date]) extends UserProfileLike with UserData