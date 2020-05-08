package com.shocktrade.common.models.user

import scala.scalajs.js

/**
 * Represents a User Profile model
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
class UserProfile(val userID: js.UndefOr[String] = js.undefined,
                  val username: js.UndefOr[String] = js.undefined,
                  val email: js.UndefOr[String] = js.undefined,
                  val equity: js.UndefOr[Double] = js.undefined,
                  val wallet: js.UndefOr[Double] = js.undefined,
                  val totalXP: js.UndefOr[Int] = js.undefined,
                  val awards: js.UndefOr[js.Array[String]] = js.undefined,
                  val lastLoginTime: js.UndefOr[js.Date] = js.undefined) extends UserProfileLike
