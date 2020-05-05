package com.shocktrade.common.models.user

import scala.scalajs.js

/**
 * Represents a User Profile model
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
class UserProfile(var userID: js.UndefOr[String] = js.undefined,
                  var username: js.UndefOr[String] = js.undefined,
                  var email: js.UndefOr[String] = js.undefined,
                  var level: js.UndefOr[Int] = js.undefined,
                  var equity: js.UndefOr[Double] = js.undefined,
                  var funds: js.UndefOr[Double] = js.undefined,
                  var wallet: js.UndefOr[Double] = js.undefined,
                  var totalXP: js.UndefOr[Int] = js.undefined,
                  var awards: js.UndefOr[js.Array[String]] = js.undefined,
                  var favoriteSymbols: js.UndefOr[js.Array[String]] = js.undefined,
                  var recentSymbols: js.UndefOr[js.Array[String]] = js.undefined,
                  var followers: js.UndefOr[js.Array[String]] = js.undefined,
                  var lastLoginTime: js.UndefOr[js.Date] = js.undefined) extends UserProfileLike

/**
 * User Profile Companion
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
object UserProfile {

  /**
   * User Profile Enrichment
   * @param profile the given [[UserProfile user profile]]
   */
  final implicit class UserProfileEnrichment(val profile: UserProfile) extends AnyVal {

    @inline
    def nextLevelXP: js.UndefOr[Int] = for {
      xp <- profile.totalXP
      nextLevelXP = (xp / 1000 + 1) * 1000
    } yield nextLevelXP

    @inline
    def userID_! : String = profile.userID.getOrElse(throw js.JavaScriptException("User ID is required"))

  }

}