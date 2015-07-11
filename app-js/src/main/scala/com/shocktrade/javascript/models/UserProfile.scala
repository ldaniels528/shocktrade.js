package com.shocktrade.javascript.models

import com.shocktrade.javascript.ScalaJsHelper._

import scala.scalajs.js

/**
 * User Profile
 * @author lawrence.daniels@gmail.com
 */
trait UserProfile extends js.Object {
  var id: js.Dynamic = js.native
  var name: String = js.native
  var facebookID: js.UndefOr[String] = js.native
  var email: js.UndefOr[String] = js.native
  var netWorth: Double = js.native
  var level: Int = js.native
  var rep: js.UndefOr[Int] = js.native
  var admin: js.UndefOr[Boolean] = js.native
  var totalXP: js.UndefOr[Int] = js.native
  var nextLevelXP: js.UndefOr[Int] = js.native
  var awards: js.Array[String] = js.native
  var favorites: js.Array[String] = js.native
  var friends: js.Array[String] = js.native
  var recentSymbols: js.Array[String] = js.native
  var lastSymbol: String = js.native
  var country: js.UndefOr[String] = js.native
  var lastLoginTime: js.UndefOr[js.Date] = js.native

}

object UserProfile {

  def apply(name: String = null,
            facebookID: js.UndefOr[String] = js.undefined,
            email: js.UndefOr[String] = null,
            netWorth: Double = 0.0d,
            level: Int = 1,
            admin: js.UndefOr[Boolean] = js.undefined,
            totalXP: Int = 0,
            awards: js.Array[String] = emptyArray,
            favorites: js.Array[String] = emptyArray,
            friends: js.Array[String] = emptyArray,
            recentSymbols: js.Array[String] = emptyArray,
            lastSymbol: String = "AAPL",
            country: js.UndefOr[String] = js.undefined,
            lastLoginTime: js.UndefOr[js.Date] = js.undefined) = {
    val profile = makeNew[UserProfile]
    profile.name = name
    profile.facebookID = facebookID
    profile.email = email
    profile.netWorth = netWorth
    profile.level = level
    profile.admin = admin
    profile.totalXP = totalXP
    profile.awards = awards
    profile.favorites = favorites
    profile.friends = friends
    profile.recentSymbols = recentSymbols
    profile.lastSymbol = lastSymbol
    profile.country = country
    profile.lastLoginTime = lastLoginTime
    profile
  }

}