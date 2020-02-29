package com.shocktrade.client.models

import com.shocktrade.common.models.user.{User, UserProfileLike}

import scala.scalajs.js

/**
  * Represents a User Profile model
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
class UserProfile(var userID: js.UndefOr[String] = js.undefined,
                  var facebookID: js.UndefOr[String] = js.undefined,
                  var username: js.UndefOr[String] = js.undefined,
                  var description: js.UndefOr[String] = js.undefined,
                  var country: js.UndefOr[String] = js.undefined,
                  var level: js.UndefOr[Int] = js.undefined,
                  var rep: js.UndefOr[Int] = js.undefined,
                  var netWorth: js.UndefOr[Double] = js.undefined,
                  var wallet: js.UndefOr[Double] = js.undefined,
                  var totalXP: js.UndefOr[Int] = js.undefined,
                  var awards: js.UndefOr[js.Array[String]] = js.undefined,
                  var favoriteSymbols: js.UndefOr[js.Array[String]] = js.undefined,
                  var recentSymbols: js.UndefOr[js.Array[String]] = js.undefined,
                  var followers: js.UndefOr[js.Array[String]] = js.undefined,
                  var friends: js.UndefOr[js.Array[String]] = js.undefined,
                  var isAdmin: js.UndefOr[Boolean] = js.undefined,
                  var lastSymbol: js.UndefOr[String] = js.undefined,
                  var lastLoginTime: js.UndefOr[js.Date] = js.undefined) extends User with UserProfileLike

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
    def nextLevelXP: js.UndefOr[Int] = profile.totalXP.map(_ + 1000)

  }

}