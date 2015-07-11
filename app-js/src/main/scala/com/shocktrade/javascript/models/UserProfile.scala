package com.shocktrade.javascript.models

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
  var admin: js.UndefOr[Boolean] = js.native
  var totalXP: Int = js.native
  var awards: js.Array[String] = js.native
  var favorites: js.Array[String] = js.native
  var friends: js.Array[String] = js.native
  var recentSymbols: js.Array[String] = js.native
  var country: js.UndefOr[String] = js.native
  var lastLoginTime: js.UndefOr[js.Date] = js.native

}
