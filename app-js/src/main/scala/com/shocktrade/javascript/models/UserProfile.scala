package com.shocktrade.javascript.models

import com.github.ldaniels528.meansjs.util.ScalaJsHelper._

import scala.scalajs.js

/**
 * User Profile
 * @author lawrence.daniels@gmail.com
 */
@js.native
trait UserProfile extends js.Object {
  var _id: js.UndefOr[BSONObjectID]
  var name: String
  var facebookID: js.UndefOr[String]
  var email: js.UndefOr[String]
  var netWorth: Double
  var level: Int
  var rep: js.UndefOr[Int]
  var admin: js.UndefOr[Boolean]
  var totalXP: js.UndefOr[Int]
  var nextLevelXP: js.UndefOr[Int]
  var awards: js.Array[String]
  var favorites: js.Array[String]
  var friends: js.Array[String]
  var recentSymbols: js.Array[String]
  var lastSymbol: String
  var country: js.UndefOr[String]
  var lastLoginTime: js.UndefOr[js.Date]
}

/**
  * User Profile Companion Object
  * @author lawrence.daniels@gmail.com
  */
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
    val profile = New[UserProfile]
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