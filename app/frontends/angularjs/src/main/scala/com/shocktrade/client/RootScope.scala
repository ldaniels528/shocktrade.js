package com.shocktrade.client

import com.shocktrade.client.models.UserProfile
import com.shocktrade.client.models.contest.{Contest, Portfolio}
import com.shocktrade.common.models.user.NetWorth
import io.scalajs.npm.angularjs.Scope

import scala.scalajs.js

/**
 * Represents the application $rootScope
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
@js.native
trait RootScope extends Scope {
  // variables
  var contest: js.UndefOr[Contest] = js.undefined
  var netWorth: js.UndefOr[NetWorth] = js.native
  var portfolio: js.UndefOr[Portfolio] = js.undefined
  var userProfile: js.UndefOr[UserProfile] = js.native

}
