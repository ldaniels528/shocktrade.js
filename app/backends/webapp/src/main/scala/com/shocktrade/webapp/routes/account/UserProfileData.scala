package com.shocktrade.webapp.routes.account

import com.shocktrade.common.models.user.UserProfileLike

import scala.scalajs.js

/**
 * Represents a User Profile data model
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
class UserProfileData(val userID: js.UndefOr[String],
                      val username: js.UndefOr[String],
                      val level: js.UndefOr[Int],
                      val equity: js.UndefOr[Double],
                      val funds: js.UndefOr[Double],
                      val wallet: js.UndefOr[Double],
                      val totalXP: js.UndefOr[Int],
                      val lastLoginTime: js.UndefOr[js.Date]) extends UserProfileLike