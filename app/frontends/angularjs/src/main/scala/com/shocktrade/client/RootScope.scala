package com.shocktrade.client

import com.shocktrade.client.models.UserProfile
import com.shocktrade.client.models.contest.Portfolio
import com.shocktrade.common.models.user.NetWorth
import io.scalajs.npm.angularjs.Scope

import scala.scalajs.js

/**
 * Represents the application $rootScope
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
@js.native
trait RootScope extends Scope {
  // functions
  var getFundsAvailable: js.Function0[js.UndefOr[Double]] = js.native

  // variables
  var netWorth: js.UndefOr[NetWorth] = js.undefined
  var portfolio: js.UndefOr[Portfolio] = js.undefined
  var userProfile: js.UndefOr[UserProfile] = js.undefined

}
