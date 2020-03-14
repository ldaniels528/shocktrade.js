package com.shocktrade.client

import com.shocktrade.client.models.UserProfile
import com.shocktrade.client.models.contest.{ContestSearchResultUI, Portfolio}
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
  var contest: js.UndefOr[ContestSearchResultUI] = js.native
  var netWorth: js.UndefOr[NetWorth] = js.native
  var portfolio: js.UndefOr[Portfolio] = js.native
  var userProfile: js.UndefOr[UserProfile] = js.native

}
